defmodule EduConsultCrm.Accounts do
  @moduledoc """
  The Accounts context for user management.
  """

  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Accounts.User
  alias EduConsultCrm.Tenants

  @doc """
  Gets a user by ID.
  """
  def get_user(id) do
    Repo.get(User, id)
  end

  @doc """
  Gets a user by ID, raises if not found.
  """
  def get_user!(id), do: Repo.get!(User, id)

  @doc """
  Gets user by email within an organization.
  """
  def get_user_by_email(email, org_id) do
    User
    |> where([u], u.email == ^email and u.organization_id == ^org_id)
    |> Repo.one()
  end

  @doc """
  Gets user by phone within an organization.
  """
  def get_user_by_phone(phone, org_id) do
    User
    |> where([u], u.phone == ^phone and u.organization_id == ^org_id)
    |> Repo.one()
  end

  @doc """
  Authenticates user by email/phone and password.
  """
  def authenticate_user(username, password, org_id) do
    user = get_user_by_email(username, org_id) || get_user_by_phone(username, org_id)

    cond do
      user && user.is_active && User.valid_password?(user, password) ->
        update_last_login(user)
        {:ok, user}

      user && !user.is_active ->
        {:error, :account_disabled}

      true ->
        Bcrypt.no_user_verify()
        {:error, :invalid_credentials}
    end
  end

  @doc """
  Creates a user with password hashing.
  """
  def create_user(attrs, org_id) do
    org = Tenants.get_organization!(org_id)

    if Tenants.user_limit_reached?(org) do
      {:error, :user_limit_reached}
    else
      result =
        %User{organization_id: org_id}
        |> User.registration_changeset(attrs)
        |> Repo.insert()

      case result do
        {:ok, user} ->
          Tenants.increment_user_count(org)
          {:ok, user}

        error ->
          error
      end
    end
  end

  @doc """
  Updates a user.
  """
  def update_user(%User{} = user, attrs) do
    user
    |> User.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Updates last login timestamp.
  """
  def update_last_login(%User{} = user) do
    user
    |> User.login_changeset()
    |> Repo.update()
  end

  @doc """
  Updates FCM token for push notifications.
  """
  def update_fcm_token(user_id, token, device_info \\ nil) do
    case get_user(user_id) do
      nil ->
        {:error, :not_found}

      user ->
        user
        |> User.fcm_changeset(%{fcm_token: token, device_info: device_info})
        |> Repo.update()
    end
  end

  @doc """
  Lists users for an organization.
  """
  def list_users(org_id) do
    User
    |> where([u], u.organization_id == ^org_id)
    |> Repo.all()
  end
end
