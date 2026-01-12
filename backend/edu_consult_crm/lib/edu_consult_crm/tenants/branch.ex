defmodule EduConsultCrm.Tenants.Branch do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "branches" do
    field :name, :string
    field :code, :string
    field :address, :string
    field :phone, :string
    field :email, :string
    field :is_active, :boolean, default: true

    belongs_to :organization, EduConsultCrm.Tenants.Organization

    timestamps(type: :utc_datetime)
  end

  def changeset(branch, attrs) do
    branch
    |> cast(attrs, [:name, :code, :address, :phone, :email, :is_active, :organization_id])
    |> validate_required([:name, :organization_id])
    |> unique_constraint([:organization_id, :code])
    |> foreign_key_constraint(:organization_id)
  end
end
