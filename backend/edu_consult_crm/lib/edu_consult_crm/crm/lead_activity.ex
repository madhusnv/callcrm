defmodule EduConsultCrm.Crm.LeadActivity do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @activity_types ~w(
    created updated deleted restored
    status_changed assigned reassigned
    note_added call_logged email_sent
    follow_up_scheduled follow_up_completed
    tag_added tag_removed
    document_uploaded
  )

  schema "lead_activities" do
    belongs_to :lead, EduConsultCrm.Crm.Lead
    belongs_to :user, EduConsultCrm.Accounts.User
    belongs_to :organization, EduConsultCrm.Tenants.Organization

    field :activity_type, :string
    field :description, :string
    field :metadata, :map, default: %{}

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(activity_type lead_id organization_id)a
  @optional_fields ~w(user_id description metadata)a

  def changeset(activity, attrs) do
    activity
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_inclusion(:activity_type, @activity_types)
    |> foreign_key_constraint(:lead_id)
    |> foreign_key_constraint(:user_id)
    |> foreign_key_constraint(:organization_id)
  end

  def activity_types, do: @activity_types
end
