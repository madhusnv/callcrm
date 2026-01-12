defmodule EduConsultCrm.Education.Institution do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "institutions" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization
    belongs_to :country, EduConsultCrm.Education.Country

    has_many :courses, EduConsultCrm.Education.Course

    field :name, :string
    field :city, :string
    field :institution_type, :string
    field :website, :string
    field :logo_url, :string
    field :display_order, :integer, default: 0
    field :is_active, :boolean, default: true

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(organization_id name)a
  @optional_fields ~w(country_id city institution_type website logo_url display_order is_active)a

  def changeset(institution, attrs) do
    institution
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_length(:name, min: 2, max: 200)
    |> foreign_key_constraint(:organization_id)
    |> foreign_key_constraint(:country_id)
    |> unique_constraint([:organization_id, :name])
  end
end
