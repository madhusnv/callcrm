defmodule EduConsultCrm.Crm.Lead do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @priorities ~w(low medium high urgent)
  @sources ~w(website phone walk_in referral social_media advertisement other)

  schema "leads" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization
    belongs_to :branch, EduConsultCrm.Tenants.Branch
    belongs_to :assigned_user, EduConsultCrm.Accounts.User, foreign_key: :assigned_to
    belongs_to :status, EduConsultCrm.Crm.LeadStatus
    belongs_to :created_user, EduConsultCrm.Accounts.User, foreign_key: :created_by

    has_many :notes, EduConsultCrm.Crm.LeadNote
    has_many :activities, EduConsultCrm.Crm.LeadActivity
    many_to_many :tags, EduConsultCrm.Crm.LeadTag, join_through: "lead_tags"

    field :first_name, :string
    field :last_name, :string
    field :phone, :string
    field :secondary_phone, :string
    field :country_code, :integer, default: 91
    field :email, :string

    field :student_name, :string
    field :parent_name, :string
    field :relationship, :string
    field :date_of_birth, :date

    field :current_education, :string
    field :current_institution, :string
    field :percentage, :decimal
    field :stream, :string
    field :graduation_year, :integer

    field :interested_courses, {:array, :string}, default: []
    field :preferred_countries, {:array, :string}, default: []
    field :preferred_institutions, {:array, :string}, default: []

    field :budget_min, :decimal
    field :budget_max, :decimal
    field :intake_preference, :string

    field :priority, :string, default: "medium"
    field :source, :string

    field :last_contact_date, :utc_datetime
    field :next_follow_up_date, :utc_datetime
    field :reminder_note, :string

    field :total_calls, :integer, default: 0
    field :total_notes, :integer, default: 0

    field :custom_fields, :map, default: %{}

    field :is_active, :boolean, default: true
    field :deleted_at, :utc_datetime

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(first_name phone organization_id status_id)a
  @optional_fields ~w(
    last_name secondary_phone country_code email
    student_name parent_name relationship date_of_birth
    current_education current_institution percentage stream graduation_year
    interested_courses preferred_countries preferred_institutions
    budget_min budget_max intake_preference
    priority source
    last_contact_date next_follow_up_date reminder_note
    total_calls total_notes custom_fields
    branch_id assigned_to created_by
    is_active deleted_at
  )a

  def changeset(lead, attrs) do
    lead
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_format(:email, ~r/@/, message: "must be a valid email")
    |> validate_inclusion(:priority, @priorities)
    |> validate_inclusion(:source, @sources ++ [nil])
    |> validate_number(:percentage, greater_than_or_equal_to: 0, less_than_or_equal_to: 100)
    |> validate_budget()
    |> foreign_key_constraint(:organization_id)
    |> foreign_key_constraint(:branch_id)
    |> foreign_key_constraint(:status_id)
    |> foreign_key_constraint(:assigned_to)
    |> foreign_key_constraint(:created_by)
    |> unique_constraint([:organization_id, :phone],
      name: :leads_organization_id_phone_index,
      message: "phone number already exists for this organization"
    )
  end

  def update_changeset(lead, attrs) do
    lead
    |> cast(attrs, @optional_fields ++ [:first_name, :phone, :status_id])
    |> validate_format(:email, ~r/@/, message: "must be a valid email")
    |> validate_inclusion(:priority, @priorities)
    |> validate_inclusion(:source, @sources ++ [nil])
    |> validate_number(:percentage, greater_than_or_equal_to: 0, less_than_or_equal_to: 100)
    |> validate_budget()
    |> foreign_key_constraint(:branch_id)
    |> foreign_key_constraint(:status_id)
    |> foreign_key_constraint(:assigned_to)
  end

  def soft_delete_changeset(lead) do
    lead
    |> change(%{
      is_active: false,
      deleted_at: DateTime.utc_now() |> DateTime.truncate(:second)
    })
  end

  def restore_changeset(lead) do
    lead
    |> change(%{
      is_active: true,
      deleted_at: nil
    })
  end

  defp validate_budget(changeset) do
    budget_min = get_field(changeset, :budget_min)
    budget_max = get_field(changeset, :budget_max)

    if budget_min && budget_max && Decimal.compare(budget_min, budget_max) == :gt do
      add_error(changeset, :budget_min, "must be less than or equal to maximum budget")
    else
      changeset
    end
  end

  def priorities, do: @priorities
  def sources, do: @sources
end
