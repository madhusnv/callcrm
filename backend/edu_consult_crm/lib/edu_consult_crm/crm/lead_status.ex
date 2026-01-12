defmodule EduConsultCrm.Crm.LeadStatus do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "lead_statuses" do
    field :name, :string
    field :code, :string
    field :color, :string, default: "#2196F3"
    field :order, :integer, default: 0
    field :is_default, :boolean, default: false
    field :is_closed, :boolean, default: false
    field :is_active, :boolean, default: true

    belongs_to :organization, EduConsultCrm.Tenants.Organization

    timestamps(type: :utc_datetime)
  end

  def changeset(status, attrs) do
    status
    |> cast(attrs, [
      :name,
      :code,
      :color,
      :order,
      :is_default,
      :is_closed,
      :is_active,
      :organization_id
    ])
    |> validate_required([:name, :code, :organization_id])
    |> unique_constraint([:organization_id, :code])
    |> foreign_key_constraint(:organization_id)
  end
end
