defmodule EduConsultCrm.Education.Course do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "courses" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization
    belongs_to :institution, EduConsultCrm.Education.Institution
    belongs_to :country, EduConsultCrm.Education.Country

    field :name, :string
    field :level, :string
    field :duration_months, :integer
    field :intake_months, {:array, :string}, default: []
    field :tuition_fee, :decimal
    field :currency_code, :string
    field :description, :string
    field :display_order, :integer, default: 0
    field :is_active, :boolean, default: true

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(organization_id name)a
  @optional_fields ~w(
    institution_id country_id level duration_months intake_months
    tuition_fee currency_code description display_order is_active
  )a

  def changeset(course, attrs) do
    course
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_length(:name, min: 2, max: 200)
    |> validate_number(:duration_months, greater_than: 0)
    |> foreign_key_constraint(:organization_id)
    |> foreign_key_constraint(:institution_id)
    |> foreign_key_constraint(:country_id)
    |> unique_constraint([:organization_id, :institution_id, :name])
  end
end
