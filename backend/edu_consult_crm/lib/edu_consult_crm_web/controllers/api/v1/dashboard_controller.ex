defmodule EduConsultCrmWeb.Api.V1.DashboardController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Dashboard
  alias EduConsultCrm.Tenants

  action_fallback EduConsultCrmWeb.FallbackController

  @doc """
  POST /api/v1/dashboard/stats
  Returns aggregated dashboard stats for the organization.
  """
  def stats(conn, _params) do
    org = conn.assigns.current_org

    case Tenants.with_org(org.id, fn -> Dashboard.get_stats(org.id) end) do
      {:ok, stats} ->
        conn
        |> put_status(:ok)
        |> json(%{status: true, data: stats})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to fetch stats"})
    end
  end
end
