defmodule EduConsultCrmWeb.Api.V1.CallLogController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Calls
  alias EduConsultCrm.Crm
  alias EduConsultCrm.Accounts.Guardian
  alias EduConsultCrm.Tenants

  action_fallback EduConsultCrmWeb.FallbackController

  @doc """
  POST /api/v1/callLog/sync
  Syncs call logs from mobile device.

  Request body:
  {
    "call_logs": [
      {
        "phone_number": "9876543210",
        "call_type": "outgoing",
        "duration": 120,
        "call_at": "2026-01-12T10:30:00Z",
        "device_call_id": "12345",
        "sim_slot": 0,
        "contact_name": "John Doe",
        "notes": "Discussed visa process"
      }
    ]
  }
  """
  def sync(conn, %{"call_logs" => call_logs}) when is_list(call_logs) do
    org = conn.assigns.current_org
    user = Guardian.Plug.current_resource(conn)

    case Tenants.with_org(org.id, fn -> Calls.sync_call_logs(org.id, user.id, call_logs) end) do
      {:ok, {:ok, results}} ->
        conn
        |> put_status(:ok)
        |> json(%{
          status: true,
          message: "Call logs synced successfully",
          data: %{
            created: results.created,
            updated: results.updated,
            leads_matched: results.matched
          }
        })

      {:ok, {:error, reason}} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{status: false, message: "Sync failed: #{inspect(reason)}"})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Sync failed"})
    end
  end

  def sync(conn, _params) do
    conn
    |> put_status(:bad_request)
    |> json(%{status: false, message: "Missing call_logs array"})
  end

  @doc """
  POST /api/v1/callLog/sync/note
  Updates notes for a call log.

  Request body:
  {
    "call_log_id": "uuid",
    "notes": "Follow up needed"
  }
  """
  def sync_notes(conn, %{"call_log_id" => call_log_id, "notes" => notes}) do
    org = conn.assigns.current_org

    result =
      Tenants.with_org(org.id, fn ->
        case Calls.get_call_log(org.id, call_log_id) do
          nil ->
            {:error, :not_found}

          call_log ->
            call_log
            |> EduConsultCrm.Calls.CallLog.update_changeset(%{"notes" => notes})
            |> EduConsultCrm.Repo.update()
        end
      end)

    case result do
      {:ok, {:ok, updated}} ->
        conn
        |> put_status(:ok)
        |> json(%{status: true, message: "Note saved", data: %{id: updated.id}})

      {:ok, {:error, :not_found}} ->
        conn
        |> put_status(:not_found)
        |> json(%{status: false, message: "Call log not found"})

      {:ok, {:error, _}} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{status: false, message: "Failed to save note"})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to save note"})
    end
  end

  @doc """
  POST /api/v1/callLog/getByLead
  Gets call history for a lead.

  Request body:
  {
    "lead_id": "uuid",
    "page": 1,
    "page_size": 20
  }
  """
  def get_by_lead(conn, %{"lead_id" => lead_id} = params) do
    org = conn.assigns.current_org
    page = Map.get(params, "page", 1)
    page_size = Map.get(params, "page_size", 20)

    result =
      Tenants.with_org(org.id, fn ->
        case Crm.get_lead_for_org(org.id, lead_id) do
          nil ->
            {:error, :not_found}

          _lead ->
            calls =
              Calls.list_calls_for_lead(org.id, lead_id, %{page: page, page_size: page_size})

            total = Calls.count_calls_for_lead(org.id, lead_id)
            {:ok, {calls, total}}
        end
      end)

    case result do
      {:ok, {:ok, {calls, total}}} ->
        conn
        |> put_status(:ok)
        |> render(:call_logs, call_logs: calls, total: total, page: page, page_size: page_size)

      {:ok, {:error, :not_found}} ->
        conn
        |> put_status(:not_found)
        |> json(%{status: false, message: "Lead not found"})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to fetch call logs"})
    end
  end

  @doc """
  POST /api/v1/callRecording/getUploadUrl
  Generates a presigned S3 URL for uploading a recording.

  Request body:
  {
    "call_log_id": "uuid",
    "file_name": "recording.mp3",
    "file_size": 1234567,
    "format": "mp3"
  }
  """
  def upload_recording(conn, %{"call_log_id" => call_log_id} = params) do
    org = conn.assigns.current_org
    user = Guardian.Plug.current_resource(conn)

    file_info = %{
      "file_name" => params["file_name"],
      "file_size" => params["file_size"],
      "format" => params["format"] || "mp3"
    }

    case Tenants.with_org(org.id, fn ->
           Calls.create_recording_upload(org.id, call_log_id, user.id, file_info)
         end) do
      {:ok, {:ok, %{recording: recording, upload_url: upload_url, storage_key: storage_key}}} ->
        conn
        |> put_status(:ok)
        |> json(%{
          status: true,
          message: "Upload URL generated",
          data: %{
            recording_id: recording.id,
            upload_url: upload_url,
            storage_key: storage_key,
            expires_in: 3600
          }
        })

      {:ok, {:error, reason}} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{status: false, message: "Failed to generate upload URL: #{inspect(reason)}"})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to generate upload URL"})
    end
  end

  @doc """
  POST /api/v1/callRecording/confirmUpload
  Confirms that a recording was uploaded successfully.

  Request body:
  {
    "recording_id": "uuid",
    "compressed_file_size": 123456,
    "duration": 120,
    "format": "mp3",
    "bitrate": 32
  }
  """
  def confirm_upload(conn, %{"recording_id" => recording_id} = params) do
    org = conn.assigns.current_org

    attrs = %{
      "compressed_file_size" => params["compressed_file_size"],
      "duration" => params["duration"],
      "format" => params["format"],
      "bitrate" => params["bitrate"]
    }

    case Tenants.with_org(org.id, fn ->
           Calls.confirm_recording_upload(org.id, recording_id, attrs)
         end) do
      {:ok, {:ok, recording}} ->
        conn
        |> put_status(:ok)
        |> json(%{
          status: true,
          message: "Recording confirmed",
          data: %{id: recording.id, status: recording.status}
        })

      {:ok, {:error, _}} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{status: false, message: "Failed to confirm upload"})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to confirm upload"})
    end
  end

  @doc """
  GET /api/v1/callRecording/stream/:id
  Returns a presigned URL for streaming/downloading a recording.
  """
  def stream_recording(conn, %{"id" => recording_id}) do
    org = conn.assigns.current_org

    case Tenants.with_org(org.id, fn -> Calls.get_recording_stream_url(org.id, recording_id) end) do
      {:ok, {:ok, url}} ->
        conn
        |> put_status(:ok)
        |> json(%{status: true, data: %{stream_url: url, expires_in: 3600}})

      {:ok, {:error, :not_available}} ->
        conn
        |> put_status(:not_found)
        |> json(%{status: false, message: "Recording not available"})

      {:ok, {:error, _}} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to generate stream URL"})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to generate stream URL"})
    end
  end
end
