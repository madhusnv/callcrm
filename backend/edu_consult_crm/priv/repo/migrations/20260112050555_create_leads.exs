defmodule EduConsultCrm.Repo.Migrations.CreateLeads do
  use Ecto.Migration

  def change do
    create table(:leads, primary_key: false) do
      add :id, :binary_id, primary_key: true

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :branch_id, references(:branches, type: :binary_id, on_delete: :nilify_all)
      add :assigned_to, references(:users, type: :binary_id, on_delete: :nilify_all)

      add :status_id, references(:lead_statuses, type: :binary_id, on_delete: :restrict),
        null: false

      add :created_by, references(:users, type: :binary_id, on_delete: :nilify_all)

      # Personal Info
      add :first_name, :string, null: false
      add :last_name, :string
      add :phone, :string, null: false
      add :secondary_phone, :string
      add :country_code, :integer, default: 91
      add :email, :string

      # Student Info
      add :student_name, :string
      add :parent_name, :string
      add :relationship, :string
      add :date_of_birth, :date

      # Education Info
      add :current_education, :string
      add :current_institution, :string
      add :percentage, :decimal
      add :stream, :string
      add :graduation_year, :integer

      # Interests
      add :interested_courses, {:array, :string}, default: []
      add :preferred_countries, {:array, :string}, default: []
      add :preferred_institutions, {:array, :string}, default: []

      # Budget
      add :budget_min, :decimal
      add :budget_max, :decimal
      add :intake_preference, :string

      # Lead Meta
      add :priority, :string, default: "medium"
      add :source, :string

      # Follow-up
      add :last_contact_date, :utc_datetime
      add :next_follow_up_date, :utc_datetime
      add :reminder_note, :text

      # Stats
      add :total_calls, :integer, default: 0
      add :total_notes, :integer, default: 0

      # Custom Fields
      add :custom_fields, :map, default: %{}

      # Soft Delete
      add :is_active, :boolean, default: true
      add :deleted_at, :utc_datetime

      timestamps(type: :utc_datetime)
    end

    create index(:leads, [:organization_id])
    create index(:leads, [:branch_id])
    create index(:leads, [:assigned_to])
    create index(:leads, [:status_id])
    create index(:leads, [:created_by])
    create index(:leads, [:phone])
    create index(:leads, [:email])
    create index(:leads, [:priority])
    create index(:leads, [:next_follow_up_date])
    create index(:leads, [:is_active])
    create unique_index(:leads, [:organization_id, :phone], where: "deleted_at IS NULL")
  end
end
