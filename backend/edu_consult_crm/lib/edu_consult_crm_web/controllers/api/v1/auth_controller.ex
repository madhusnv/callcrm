defmodule EduConsultCrmWeb.Api.V1.AuthController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Accounts
  alias EduConsultCrm.Accounts.Guardian

  action_fallback EduConsultCrmWeb.FallbackController

  def login(conn, %{"username" => username, "password" => password}) do
    org = conn.assigns[:current_org]

    case Accounts.authenticate_user(username, password, org.id) do
      {:ok, user} ->
        {:ok, access_token, _claims} = Guardian.encode_and_sign(user, %{}, ttl: {1, :day})

        {:ok, refresh_token, _claims} =
          Guardian.encode_and_sign(user, %{typ: "refresh"}, ttl: {30, :day})

        conn
        |> put_status(:ok)
        |> json(%{
          status: true,
          data: %{
            access_token: access_token,
            refresh_token: refresh_token,
            user: serialize_user(user)
          }
        })

      {:error, :invalid_credentials} ->
        conn
        |> put_status(:unauthorized)
        |> json(%{status: false, message: "Invalid credentials"})

      {:error, :account_disabled} ->
        conn
        |> put_status(:forbidden)
        |> json(%{status: false, message: "Account is disabled"})
    end
  end

  def refresh(conn, %{"refresh_token" => refresh_token}) do
    case Guardian.decode_and_verify(refresh_token, %{typ: "refresh"}) do
      {:ok, claims} ->
        case Guardian.resource_from_claims(claims) do
          {:ok, user} ->
            {:ok, new_access_token, _claims} = Guardian.encode_and_sign(user, %{}, ttl: {1, :day})

            conn
            |> put_status(:ok)
            |> json(%{
              status: true,
              data: %{access_token: new_access_token}
            })

          {:error, _} ->
            conn
            |> put_status(:unauthorized)
            |> json(%{status: false, message: "Invalid refresh token"})
        end

      {:error, _} ->
        conn
        |> put_status(:unauthorized)
        |> json(%{status: false, message: "Invalid refresh token"})
    end
  end

  def register(conn, params) do
    org = conn.assigns[:current_org]

    case Accounts.create_user(params, org.id) do
      {:ok, user} ->
        {:ok, access_token, _claims} = Guardian.encode_and_sign(user, %{}, ttl: {1, :day})

        {:ok, refresh_token, _claims} =
          Guardian.encode_and_sign(user, %{typ: "refresh"}, ttl: {30, :day})

        conn
        |> put_status(:created)
        |> json(%{
          status: true,
          data: %{
            access_token: access_token,
            refresh_token: refresh_token,
            user: serialize_user(user)
          }
        })

      {:error, :user_limit_reached} ->
        conn
        |> put_status(:payment_required)
        |> json(%{status: false, message: "User limit reached. Please upgrade your plan."})

      {:error, changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> json(%{status: false, message: "Validation failed", errors: format_errors(changeset)})
    end
  end

  def send_otp(conn, %{"phone" => phone}) do
    otp = :rand.uniform(999_999) |> Integer.to_string() |> String.pad_leading(6, "0")

    # In production, send via SMS service and store in Redis/cache
    # For now, just log it
    require Logger
    Logger.info("OTP for #{phone}: #{otp}")

    conn
    |> put_status(:ok)
    |> json(%{status: true, message: "OTP sent successfully"})
  end

  def verify_otp(conn, %{"phone" => _phone, "otp" => _otp}) do
    # In production, verify against cached OTP
    # For dev, accept any 6-digit OTP
    conn
    |> put_status(:ok)
    |> json(%{status: true, data: %{verified: true}})
  end

  def logout(conn, _params) do
    user = Guardian.Plug.current_resource(conn)

    if user do
      Accounts.update_fcm_token(user.id, nil)
    end

    conn
    |> Guardian.Plug.sign_out()
    |> put_status(:ok)
    |> json(%{status: true, message: "Logged out successfully"})
  end

  defp serialize_user(user) do
    %{
      id: user.id,
      email: user.email,
      phone: user.phone,
      first_name: user.first_name,
      last_name: user.last_name,
      role: user.role,
      organization_id: user.organization_id,
      branch_id: user.branch_id
    }
  end

  defp format_errors(changeset) do
    Ecto.Changeset.traverse_errors(changeset, fn {msg, opts} ->
      Enum.reduce(opts, msg, fn {key, value}, acc ->
        String.replace(acc, "%{#{key}}", to_string(value))
      end)
    end)
  end
end
