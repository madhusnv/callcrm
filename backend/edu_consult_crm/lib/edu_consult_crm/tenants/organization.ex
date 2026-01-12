defmodule EduConsultCrm.Tenants.Organization do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "organizations" do
    field :name, :string
    field :slug, :string
    field :api_key, :string
    field :api_secret, :string
    field :api_key_generated_at, :utc_datetime

    field :email, :string
    field :phone, :string
    field :address, :string
    field :logo_url, :string

    field :settings, :map, default: %{}
    field :features, {:array, :string}, default: []

    field :plan, :string, default: "free"
    field :max_users, :integer, default: 5
    field :max_leads, :integer, default: 500
    field :subscription_status, :string, default: "active"
    field :subscription_expires_at, :utc_datetime

    field :current_users_count, :integer, default: 0
    field :current_leads_count, :integer, default: 0
    field :storage_used_bytes, :integer, default: 0
    field :storage_limit_bytes, :integer, default: 1_073_741_824

    field :is_active, :boolean, default: true
    field :suspended_at, :utc_datetime
    field :suspension_reason, :string

    has_many :users, EduConsultCrm.Accounts.User
    has_many :branches, EduConsultCrm.Tenants.Branch
    has_many :lead_statuses, EduConsultCrm.Crm.LeadStatus

    timestamps(type: :utc_datetime)
  end

  def changeset(org, attrs) do
    org
    |> cast(attrs, [
      :name,
      :slug,
      :email,
      :phone,
      :address,
      :logo_url,
      :settings,
      :features,
      :plan,
      :max_users,
      :max_leads
    ])
    |> validate_required([:name, :slug])
    |> unique_constraint(:slug)
    |> unique_constraint(:api_key)
    |> maybe_generate_api_key()
  end

  defp maybe_generate_api_key(changeset) do
    if get_field(changeset, :api_key) do
      changeset
    else
      changeset
      |> put_change(:api_key, generate_api_key())
      |> put_change(:api_key_generated_at, DateTime.utc_now() |> DateTime.truncate(:second))
    end
  end

  def generate_api_key do
    "org_" <> Base.encode32(:crypto.strong_rand_bytes(20), case: :lower, padding: false)
  end
end
