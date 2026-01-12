defmodule EduConsultCrmWeb.Api.V1.EmployeeController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Accounts
  alias EduConsultCrm.Accounts.Guardian
  alias EduConsultCrm.Tenants

  action_fallback EduConsultCrmWeb.FallbackController

  @doc """
  POST /api/v1/employee/updateFCM
  Updates the FCM token for the authenticated user.
  """
  def update_fcm(conn, %{"token" => token} = params) do
    org = conn.assigns.current_org

    case Guardian.Plug.current_resource(conn) do
      nil ->
        {:error, :unauthorized}

      user ->
        device_info = Map.get(params, "deviceInfo")

        case Tenants.with_org(org.id, fn ->
               Accounts.update_fcm_token(user.id, token, device_info)
             end) do
          {:ok, {:ok, updated}} ->
            conn
            |> put_status(:ok)
            |> json(%{
              status: true,
              data: %{
                userId: updated.id,
                fcmToken: updated.fcm_token
              }
            })

          {:ok, {:error, :not_found}} ->
            {:error, :not_found}

          {:ok, {:error, changeset}} ->
            {:error, changeset}

          {:error, _} ->
            conn
            |> put_status(:internal_server_error)
            |> json(%{status: false, message: "Failed to update token"})
        end
    end
  end
end
