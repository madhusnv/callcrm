defmodule EduConsultCrm.Calls do
  @moduledoc """
  The Calls context - handles call log sync, recordings, and lead matching.
  All queries are scoped by organization_id for multi-tenancy.
  """

  import Ecto.Query, warn: false
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Calls.{CallLog, CallRecording}
  alias EduConsultCrm.Crm
  alias EduConsultCrm.Crm.Lead

  # =============================================================================
  # Call Log CRUD
  # =============================================================================

  @doc """
  Gets a call log by ID, scoped to organization.
  """
  def get_call_log(org_id, id) do
    CallLog
    |> where([c], c.organization_id == ^org_id and c.id == ^id)
    |> Repo.one()
  end

  def get_call_log!(org_id, id) do
    CallLog
    |> where([c], c.organization_id == ^org_id and c.id == ^id)
    |> Repo.one!()
  end

  @doc """
  Lists call logs for an organization with optional filters.
  """
  def list_call_logs(org_id, params \\ %{}) do
    page = Map.get(params, :page, 1)
    page_size = Map.get(params, :page_size, 20)
    offset = (page - 1) * page_size

    CallLog
    |> where([c], c.organization_id == ^org_id)
    |> apply_call_filters(params)
    |> order_by([c], desc: c.call_at)
    |> limit(^page_size)
    |> offset(^offset)
    |> preload([:lead, :user, :recording])
    |> Repo.all()
  end

  @doc """
  Lists call logs for a specific lead.
  """
  def list_calls_for_lead(org_id, lead_id, params \\ %{}) do
    page = Map.get(params, :page, 1)
    page_size = Map.get(params, :page_size, 20)
    offset = (page - 1) * page_size

    CallLog
    |> where([c], c.organization_id == ^org_id and c.lead_id == ^lead_id)
    |> order_by([c], desc: c.call_at)
    |> limit(^page_size)
    |> offset(^offset)
    |> preload([:user, :recording])
    |> Repo.all()
  end

  @doc """
  Counts call logs for a lead.
  """
  def count_calls_for_lead(org_id, lead_id) do
    CallLog
    |> where([c], c.organization_id == ^org_id and c.lead_id == ^lead_id)
    |> Repo.aggregate(:count, :id)
  end

  # =============================================================================
  # Call Log Sync
  # =============================================================================

  @doc """
  Syncs call logs from mobile device. Creates or updates based on device_call_id.
  Returns {:ok, %{created: count, updated: count, matched: count}}
  """
  def sync_call_logs(org_id, user_id, call_logs) when is_list(call_logs) do
    results =
      Enum.reduce(call_logs, %{created: 0, updated: 0, matched: 0}, fn log_data, acc ->
        case sync_single_call(org_id, user_id, log_data) do
          {:created, lead_matched} ->
            acc
            |> Map.update!(:created, &(&1 + 1))
            |> maybe_increment(:matched, lead_matched)

          {:updated, _} ->
            Map.update!(acc, :updated, &(&1 + 1))

          {:skipped, _} ->
            acc
        end
      end)

    {:ok, results}
  end

  defp sync_single_call(org_id, user_id, attrs) do
    device_call_id = attrs["device_call_id"] || attrs[:device_call_id]

    existing =
      if device_call_id do
        CallLog
        |> where([c], c.user_id == ^user_id and c.device_call_id == ^device_call_id)
        |> Repo.one()
      end

    if existing do
      # Update notes if provided
      case existing |> CallLog.update_changeset(attrs) |> Repo.update() do
        {:ok, _} -> {:updated, false}
        {:error, _} -> {:skipped, :update_failed}
      end
    else
      # Create new call log
      call_attrs =
        attrs
        |> Map.put("organization_id", org_id)
        |> Map.put("user_id", user_id)

      # Try to match to a lead
      phone = call_attrs["phone_number"] || call_attrs[:phone_number]
      {lead_id, matched} = match_phone_to_lead(org_id, phone)
      call_attrs = Map.put(call_attrs, "lead_id", lead_id)

      case %CallLog{} |> CallLog.changeset(call_attrs) |> Repo.insert() do
        {:ok, call_log} ->
          # Update lead stats if matched
          if lead_id do
            update_lead_call_stats(lead_id, call_log)
          end

          {:created, matched}

        {:error, _} ->
          {:skipped, :insert_failed}
      end
    end
  end

  defp maybe_increment(acc, key, true), do: Map.update!(acc, key, &(&1 + 1))
  defp maybe_increment(acc, _key, false), do: acc

  @doc """
  Matches a phone number to an existing lead in the organization.
  Returns {lead_id | nil, matched?}
  """
  def match_phone_to_lead(org_id, phone) when is_binary(phone) do
    # Normalize phone number - remove spaces, dashes, country code prefix
    normalized = normalize_phone(phone)

    lead =
      Lead
      |> where([l], l.organization_id == ^org_id)
      |> where([l], is_nil(l.deleted_at))
      |> where(
        [l],
        fragment(
          "REPLACE(REPLACE(REPLACE(?, ' ', ''), '-', ''), '+91', '') = ?",
          l.phone,
          ^normalized
        ) or
          fragment(
            "REPLACE(REPLACE(REPLACE(?, ' ', ''), '-', ''), '+91', '') = ?",
            l.secondary_phone,
            ^normalized
          )
      )
      |> limit(1)
      |> Repo.one()

    if lead, do: {lead.id, true}, else: {nil, false}
  end

  def match_phone_to_lead(_org_id, _phone), do: {nil, false}

  defp normalize_phone(phone) do
    phone
    |> String.replace(~r/[\s\-\(\)]/, "")
    |> String.replace(~r/^\+91/, "")
    |> String.replace(~r/^91(?=\d{10}$)/, "")
  end

  @doc """
  Updates lead's call statistics after a new call is logged.
  """
  def update_lead_call_stats(lead_id, %CallLog{} = call_log) do
    lead = Crm.get_lead!(lead_id)

    Crm.update_lead(lead, nil, %{
      "total_calls" => (lead.total_calls || 0) + 1,
      "last_contact_date" => call_log.call_at
    })
  end

  # =============================================================================
  # Call Recordings - S3 Integration
  # =============================================================================

  @doc """
  Creates a recording record and generates a presigned S3 upload URL.
  Returns {:ok, %{recording: recording, upload_url: url, storage_key: key}}
  """
  def create_recording_upload(org_id, call_log_id, user_id, file_info \\ %{}) do
    call_log = get_call_log!(org_id, call_log_id)

    # Generate S3 key
    timestamp = DateTime.utc_now() |> DateTime.to_unix()
    file_ext = file_info["format"] || "mp3"
    storage_key = "recordings/#{org_id}/#{user_id}/#{call_log_id}_#{timestamp}.#{file_ext}"

    bucket = get_s3_bucket()

    attrs = %{
      organization_id: org_id,
      call_log_id: call_log.id,
      user_id: user_id,
      original_file_name: file_info["file_name"],
      original_file_size: file_info["file_size"],
      format: file_ext,
      storage_key: storage_key,
      storage_bucket: bucket,
      expires_at: DateTime.utc_now() |> DateTime.add(90, :day) |> DateTime.truncate(:second)
    }

    with {:ok, recording} <- create_recording(attrs),
         {:ok, upload_url} <- generate_presigned_upload_url(storage_key) do
      {:ok,
       %{
         recording: recording,
         upload_url: upload_url,
         storage_key: storage_key
       }}
    end
  end

  @doc """
  Creates a recording record.
  """
  def create_recording(attrs) do
    %CallRecording{}
    |> CallRecording.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Confirms a recording upload was successful.
  """
  def confirm_recording_upload(org_id, recording_id, attrs \\ %{}) do
    recording = get_recording!(org_id, recording_id)

    recording
    |> CallRecording.confirm_changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Marks a recording upload as failed.
  """
  def fail_recording_upload(org_id, recording_id, error) do
    recording = get_recording!(org_id, recording_id)

    recording
    |> CallRecording.fail_changeset(error)
    |> Repo.update()
  end

  @doc """
  Gets a recording by ID, scoped to organization.
  """
  def get_recording(org_id, id) do
    CallRecording
    |> where([r], r.organization_id == ^org_id and r.id == ^id)
    |> Repo.one()
  end

  def get_recording!(org_id, id) do
    CallRecording
    |> where([r], r.organization_id == ^org_id and r.id == ^id)
    |> Repo.one!()
  end

  @doc """
  Generates a presigned URL for streaming/downloading a recording.
  """
  def get_recording_stream_url(org_id, recording_id) do
    recording = get_recording!(org_id, recording_id)

    if recording.status == "uploaded" and recording.storage_key do
      generate_presigned_download_url(recording.storage_key)
    else
      {:error, :not_available}
    end
  end

  # =============================================================================
  # S3 Helpers
  # =============================================================================

  defp get_s3_bucket do
    Application.get_env(:edu_consult_crm, :s3)[:bucket] ||
      System.get_env("AWS_S3_BUCKET") ||
      "edu-consult-recordings"
  end

  defp generate_presigned_upload_url(storage_key) do
    config = ExAws.Config.new(:s3)
    bucket = get_s3_bucket()

    # Generate presigned PUT URL valid for 1 hour
    {:ok, url} = ExAws.S3.presigned_url(config, :put, bucket, storage_key, expires_in: 3600)
    {:ok, url}
  rescue
    _ -> {:error, :s3_error}
  end

  defp generate_presigned_download_url(storage_key) do
    config = ExAws.Config.new(:s3)
    bucket = get_s3_bucket()

    # Generate presigned GET URL valid for 1 hour
    {:ok, url} = ExAws.S3.presigned_url(config, :get, bucket, storage_key, expires_in: 3600)
    {:ok, url}
  rescue
    _ -> {:error, :s3_error}
  end

  # =============================================================================
  # Filters
  # =============================================================================

  defp apply_call_filters(query, params) do
    query
    |> filter_by_user(params)
    |> filter_by_lead(params)
    |> filter_by_type(params)
    |> filter_by_date_range(params)
  end

  defp filter_by_user(query, %{user_id: user_id}) when not is_nil(user_id) do
    where(query, [c], c.user_id == ^user_id)
  end

  defp filter_by_user(query, _), do: query

  defp filter_by_lead(query, %{lead_id: lead_id}) when not is_nil(lead_id) do
    where(query, [c], c.lead_id == ^lead_id)
  end

  defp filter_by_lead(query, _), do: query

  defp filter_by_type(query, %{call_type: call_type})
       when call_type in ~w(incoming outgoing missed) do
    where(query, [c], c.call_type == ^call_type)
  end

  defp filter_by_type(query, _), do: query

  defp filter_by_date_range(query, %{start_date: start_date, end_date: end_date})
       when not is_nil(start_date) and not is_nil(end_date) do
    where(query, [c], c.call_at >= ^start_date and c.call_at <= ^end_date)
  end

  defp filter_by_date_range(query, _), do: query
end
