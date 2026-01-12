defmodule EduConsultCrm.Accounts.AuthErrorHandler do
  import Plug.Conn

  @behaviour Guardian.Plug.ErrorHandler

  @impl Guardian.Plug.ErrorHandler
  def auth_error(conn, {type, _reason}, _opts) do
    body = Jason.encode!(%{status: false, message: error_message(type)})

    conn
    |> put_resp_content_type("application/json")
    |> send_resp(:unauthorized, body)
  end

  defp error_message(:unauthenticated), do: "Authentication required"
  defp error_message(:invalid_token), do: "Invalid or expired token"
  defp error_message(_), do: "Authentication failed"
end
