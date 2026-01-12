defmodule EduConsultCrm.Notifications.FcmSender do
  @moduledoc """
  Sends push notifications via FCM legacy HTTP API.
  """

  require Logger

  @fcm_url "https://fcm.googleapis.com/fcm/send"

  def send(token, %{title: title, body: body, data: data} = _payload) when is_binary(token) do
    case server_key() do
      nil ->
        Logger.warning("FCM server key not configured; skipping notification")
        :ok

      key ->
        headers = [
          {"authorization", "key=#{key}"},
          {"content-type", "application/json"}
        ]

        body =
          %{
            to: token,
            notification: %{
              title: title,
              body: body
            },
            data: data
          }

        case Req.post(@fcm_url, headers: headers, json: body) do
          {:ok, %{status: 200}} ->
            :ok

          {:ok, %{status: status, body: resp}} ->
            Logger.warning("FCM request failed with status #{status}: #{inspect(resp)}")
            :error

          {:error, reason} ->
            Logger.warning("FCM request error: #{inspect(reason)}")
            :error
        end
    end
  end

  def send(_token, _payload), do: :ok

  defp server_key do
    Application.get_env(:edu_consult_crm, :fcm)[:server_key] ||
      System.get_env("FCM_SERVER_KEY")
  end
end
