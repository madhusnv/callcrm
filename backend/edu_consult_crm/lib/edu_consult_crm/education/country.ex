defmodule EduConsultCrm.Education.Country do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "countries" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization

    field :name, :string
    field :code, :string
    field :currency_code, :string
    field :phone_code, :string
    field :display_order, :integer, default: 0
    field :is_active, :boolean, default: true

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(organization_id name code)a
  @optional_fields ~w(currency_code phone_code display_order is_active)a

  def changeset(country, attrs) do
    country
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_length(:code, min: 2, max: 3)
    |> validate_length(:name, min: 2, max: 100)
    |> foreign_key_constraint(:organization_id)
    |> unique_constraint([:organization_id, :code])
    |> unique_constraint([:organization_id, :name])
  end
end
