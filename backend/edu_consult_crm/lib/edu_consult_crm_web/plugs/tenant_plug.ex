defmodule EduConsultCrmWeb.Plugs.TenantPlug do
  @moduledoc """
  Plug to resolve tenant from X-API-Key header.
  """

  import Plug.Conn
  alias EduConsultCrm.Tenants

  def init(opts), do: opts

  def call(conn, _opts) do
    case get_req_header(conn, "x-api-key") do
      [api_key] ->
        resolve_tenant(conn, api_key)

      [] ->
        send_error(conn, :bad_request, "X-API-Key header is required")
    end
  end

  defp resolve_tenant(conn, api_key) do
    case Tenants.get_organization_by_api_key(api_key) do
      {:ok, org} ->
        # Store in process dictionary for scoped queries
        Process.put(:current_org_id, org.id)

        conn
        |> assign(:current_org, org)

      {:error, :invalid_api_key} ->
        send_error(conn, :unauthorized, "Invalid API key")

      {:error, {:suspended, reason}} ->
        send_error(conn, :forbidden, "Account suspended: #{reason || "Contact support"}")

      {:error, :subscription_expired} ->
        send_error(conn, :payment_required, "Subscription expired")
    end
  end

  defp send_error(conn, status, message) do
    body = Jason.encode!(%{status: false, message: message})

    conn
    |> put_resp_content_type("application/json")
    |> send_resp(status, body)
    |> halt()
  end
end
