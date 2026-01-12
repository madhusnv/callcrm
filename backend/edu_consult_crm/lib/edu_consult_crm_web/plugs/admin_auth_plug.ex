defmodule EduConsultCrmWeb.Plugs.AdminAuthPlug do
  @moduledoc """
  Basic auth guard for admin LiveViews. Fails closed if not configured.
  """

  import Plug.Conn

  def init(opts), do: opts

  def call(conn, _opts) do
    case credentials() do
      {:ok, {expected_user, expected_pass}} ->
        case parse_basic_auth(conn) do
          {:ok, {user, pass}} ->
            if secure_compare(user, expected_user) and secure_compare(pass, expected_pass) do
              conn
            else
              unauthorized(conn)
            end

          _ ->
            unauthorized(conn)
        end

      :error ->
        conn
        |> send_resp(:forbidden, "Admin access disabled")
        |> halt()
    end
  end

  defp credentials do
    config = Application.get_env(:edu_consult_crm, :admin_auth, %{})
    user = Map.get(config, :username)
    pass = Map.get(config, :password)

    if is_binary(user) and is_binary(pass) and user != "" and pass != "" do
      {:ok, {user, pass}}
    else
      :error
    end
  end

  defp parse_basic_auth(conn) do
    case get_req_header(conn, "authorization") do
      ["Basic " <> encoded] ->
        with {:ok, decoded} <- Base.decode64(encoded),
             [user, pass] <- String.split(decoded, ":", parts: 2) do
          {:ok, {user, pass}}
        else
          _ -> :error
        end

      _ ->
        :error
    end
  end

  defp unauthorized(conn) do
    conn
    |> put_resp_header("www-authenticate", ~s(Basic realm="admin"))
    |> send_resp(:unauthorized, "Unauthorized")
    |> halt()
  end

  defp secure_compare(left, right) do
    Plug.Crypto.secure_compare(left, right)
  end
end
