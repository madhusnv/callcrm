defmodule EduConsultCrm.Templates.MessageTemplate do
  @moduledoc """
  Schema for WhatsApp/SMS message templates.
  """
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @categories ~w(greeting follow_up offer visa_info general)

  schema "message_templates" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization

    field :name, :string
    field :content, :string
    field :category, :string
    field :dynamic_fields, {:array, :string}, default: []
    field :whatsapp_enabled, :boolean, default: true
    field :sms_enabled, :boolean, default: true
    field :order, :integer, default: 0
    field :is_active, :boolean, default: true

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(name content organization_id)a
  @optional_fields ~w(category dynamic_fields whatsapp_enabled sms_enabled order is_active)a

  def changeset(template, attrs) do
    template
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_inclusion(:category, @categories ++ [nil])
    |> extract_dynamic_fields()
    |> foreign_key_constraint(:organization_id)
  end

  # Extracts dynamic fields like {{name}}, {{course}} from content
  defp extract_dynamic_fields(changeset) do
    content = get_field(changeset, :content)
    
    if content do
      fields = Regex.scan(~r/\{\{(\w+)\}\}/, content)
               |> Enum.map(fn [_full, field] -> field end)
               |> Enum.uniq()
      
      put_change(changeset, :dynamic_fields, fields)
    else
      changeset
    end
  end

  def categories, do: @categories
end
