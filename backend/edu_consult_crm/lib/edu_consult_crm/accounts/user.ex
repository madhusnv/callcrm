defmodule EduConsultCrm.Accounts.User do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @roles ~w(admin manager counselor agent)

  schema "users" do
    field :email, :string
    field :phone, :string
    field :password, :string, virtual: true, redact: true
    field :password_hash, :string, redact: true

    field :first_name, :string
    field :last_name, :string
    field :role, :string, default: "agent"
    field :avatar_url, :string

    field :fcm_token, :string
    field :device_info, :map

    field :last_login_at, :utc_datetime
    field :is_active, :boolean, default: true

    belongs_to :organization, EduConsultCrm.Tenants.Organization
    belongs_to :branch, EduConsultCrm.Tenants.Branch

    timestamps(type: :utc_datetime)
  end

  def registration_changeset(user, attrs) do
    user
    |> cast(attrs, [:email, :phone, :password, :first_name, :last_name, :role, :branch_id])
    |> validate_required([:email, :phone, :password, :first_name])
    |> validate_email()
    |> validate_password()
    |> validate_inclusion(:role, @roles)
    |> hash_password()
  end

  def changeset(user, attrs) do
    user
    |> cast(attrs, [:first_name, :last_name, :phone, :avatar_url, :is_active, :branch_id])
    |> validate_required([:first_name])
  end

  def fcm_changeset(user, attrs) do
    user
    |> cast(attrs, [:fcm_token, :device_info])
  end

  def login_changeset(user) do
    user
    |> change(%{last_login_at: DateTime.utc_now()})
  end

  defp validate_email(changeset) do
    changeset
    |> validate_required([:email])
    |> validate_format(:email, ~r/^[^\s]+@[^\s]+$/, message: "must be a valid email")
    |> validate_length(:email, max: 160)
    |> unique_constraint([:email, :organization_id])
  end

  defp validate_password(changeset) do
    changeset
    |> validate_required([:password])
    |> validate_length(:password, min: 6, max: 72)
  end

  defp hash_password(changeset) do
    case changeset do
      %Ecto.Changeset{valid?: true, changes: %{password: password}} ->
        put_change(changeset, :password_hash, Bcrypt.hash_pwd_salt(password))

      _ ->
        changeset
    end
  end

  def valid_password?(%__MODULE__{password_hash: hash}, password)
      when is_binary(hash) and byte_size(password) > 0 do
    Bcrypt.verify_pass(password, hash)
  end

  def valid_password?(_, _), do: Bcrypt.no_user_verify()
end
