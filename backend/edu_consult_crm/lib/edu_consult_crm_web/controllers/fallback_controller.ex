defmodule EduConsultCrmWeb.FallbackController do
  use EduConsultCrmWeb, :controller

  def call(conn, {:error, %Ecto.Changeset{} = changeset}) do
    errors =
      Ecto.Changeset.traverse_errors(changeset, fn {msg, opts} ->
        Enum.reduce(opts, msg, fn {key, value}, acc ->
          String.replace(acc, "%{#{key}}", to_string(value))
        end)
      end)

    conn
    |> put_status(:unprocessable_entity)
    |> json(%{status: false, message: "Validation failed", errors: errors})
  end

  def call(conn, {:error, :not_found}) do
    conn
    |> put_status(:not_found)
    |> json(%{status: false, message: "Resource not found"})
  end

  def call(conn, {:error, :unauthorized}) do
    conn
    |> put_status(:unauthorized)
    |> json(%{status: false, message: "Unauthorized"})
  end

  def call(conn, {:error, :leads_limit_reached}) do
    conn
    |> put_status(:payment_required)
    |> json(%{status: false, message: "Lead limit reached. Please upgrade your plan."})
  end

  def call(conn, {:error, :forbidden}) do
    conn
    |> put_status(:forbidden)
    |> json(%{status: false, message: "Access denied"})
  end

  def call(conn, {:error, message}) when is_binary(message) do
    conn
    |> put_status(:bad_request)
    |> json(%{status: false, message: message})
  end
end
