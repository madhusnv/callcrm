defmodule EduConsultCrmWeb.Plugs.EnsureUserOrgMatch do
  @moduledoc """
  Plug to ensure the authenticated user belongs to the tenant resolved from API key.
  
  This prevents cross-tenant attacks where an attacker with a valid JWT for org A
  uses an API key for org B to access org B's data.
  """

  import Plug.Conn
  alias EduConsultCrm.Accounts.Guardian

  def init(opts), do: opts

  def call(conn, _opts) do
    user = Guardian.Plug.current_resource(conn)
    org = conn.assigns[:current_org]

    cond do
      is_nil(user) or is_nil(org) ->
        conn

      user.organization_id == org.id ->
        conn

      true ->
        send_forbidden(conn)
    end
  end

  defp send_forbidden(conn) do
    body = Jason.encode!(%{
      status: false,
      message: "Access denied"
    })

    conn
    |> put_resp_content_type("application/json")
    |> send_resp(:forbidden, body)
    |> halt()
  end
end
