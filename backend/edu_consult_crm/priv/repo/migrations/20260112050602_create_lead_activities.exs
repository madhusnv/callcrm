defmodule EduConsultCrm.Repo.Migrations.CreateLeadActivities do
  use Ecto.Migration

  def change do
    create table(:lead_activities, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :lead_id, references(:leads, type: :binary_id, on_delete: :delete_all), null: false
      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all)

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :activity_type, :string, null: false
      add :description, :text
      add :metadata, :map, default: %{}

      timestamps(type: :utc_datetime)
    end

    create index(:lead_activities, [:lead_id])
    create index(:lead_activities, [:user_id])
    create index(:lead_activities, [:organization_id])
    create index(:lead_activities, [:activity_type])
    create index(:lead_activities, [:inserted_at])
  end
end
