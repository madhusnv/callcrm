defmodule EduConsultCrm.Crm.LeadTag do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "tags" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization

    field :name, :string
    field :color, :string, default: "#2196F3"
    field :is_active, :boolean, default: true

    many_to_many :leads, EduConsultCrm.Crm.Lead, join_through: "lead_tags"

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(name organization_id)a
  @optional_fields ~w(color is_active)a

  def changeset(tag, attrs) do
    tag
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_length(:name, min: 1, max: 50)
    |> validate_format(:color, ~r/^#[0-9A-Fa-f]{6}$/, message: "must be a valid hex color")
    |> foreign_key_constraint(:organization_id)
    |> unique_constraint([:organization_id, :name])
  end
end
