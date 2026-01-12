defmodule EduConsultCrm.Templates.NoteTemplate do
  @moduledoc """
  Schema for quick note templates used after calls.
  """
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @categories ~w(call follow_up general status_change)

  schema "note_templates" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization

    field :name, :string
    field :content, :string
    field :category, :string
    field :shortcut, :string
    field :order, :integer, default: 0
    field :is_active, :boolean, default: true

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(name content organization_id)a
  @optional_fields ~w(category shortcut order is_active)a

  def changeset(template, attrs) do
    template
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_inclusion(:category, @categories ++ [nil])
    |> foreign_key_constraint(:organization_id)
  end

  def categories, do: @categories
end
