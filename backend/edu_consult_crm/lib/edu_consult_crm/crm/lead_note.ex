defmodule EduConsultCrm.Crm.LeadNote do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @note_types ~w(general call meeting email follow_up)

  schema "lead_notes" do
    belongs_to :lead, EduConsultCrm.Crm.Lead
    belongs_to :user, EduConsultCrm.Accounts.User

    field :call_log_id, :binary_id
    field :content, :string
    field :note_type, :string, default: "general"
    field :is_pinned, :boolean, default: false

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(content lead_id)a
  @optional_fields ~w(user_id call_log_id note_type is_pinned)a

  def changeset(note, attrs) do
    note
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_inclusion(:note_type, @note_types)
    |> validate_length(:content, min: 1, max: 10_000)
    |> foreign_key_constraint(:lead_id)
    |> foreign_key_constraint(:user_id)
  end

  def note_types, do: @note_types
end
