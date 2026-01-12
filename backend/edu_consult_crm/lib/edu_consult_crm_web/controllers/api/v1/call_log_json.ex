defmodule EduConsultCrmWeb.Api.V1.CallLogJSON do
  @moduledoc """
  JSON rendering for call log responses.
  """

  alias EduConsultCrm.Calls.{CallLog, CallRecording}

  def call_logs(%{call_logs: call_logs, total: total, page: page, page_size: page_size}) do
    %{
      status: true,
      data: %{
        call_logs: Enum.map(call_logs, &call_log_data/1),
        pagination: %{
          total: total,
          page: page,
          page_size: page_size,
          total_pages: ceil(total / max(page_size, 1))
        }
      }
    }
  end

  def call_log(%{call_log: call_log}) do
    %{
      status: true,
      data: call_log_data(call_log)
    }
  end

  defp call_log_data(%CallLog{} = call_log) do
    %{
      id: call_log.id,
      phone_number: call_log.phone_number,
      call_type: call_log.call_type,
      duration: call_log.duration,
      call_at: call_log.call_at,
      sim_slot: call_log.sim_slot,
      device_call_id: call_log.device_call_id,
      contact_name: call_log.contact_name,
      notes: call_log.notes,
      lead_id: call_log.lead_id,
      user_id: call_log.user_id,
      lead: lead_summary(call_log.lead),
      user: user_summary(call_log.user),
      recording: recording_data(call_log.recording),
      inserted_at: call_log.inserted_at,
      updated_at: call_log.updated_at
    }
  end

  defp lead_summary(nil), do: nil
  defp lead_summary(%Ecto.Association.NotLoaded{}), do: nil
  defp lead_summary(lead) do
    %{
      id: lead.id,
      first_name: lead.first_name,
      last_name: lead.last_name,
      phone: lead.phone
    }
  end

  defp user_summary(nil), do: nil
  defp user_summary(%Ecto.Association.NotLoaded{}), do: nil
  defp user_summary(user) do
    %{
      id: user.id,
      first_name: user.first_name,
      last_name: user.last_name
    }
  end

  defp recording_data(nil), do: nil
  defp recording_data(%Ecto.Association.NotLoaded{}), do: nil
  defp recording_data(%CallRecording{} = recording) do
    %{
      id: recording.id,
      status: recording.status,
      duration: recording.duration,
      format: recording.format,
      file_size: recording.compressed_file_size || recording.original_file_size
    }
  end
end
