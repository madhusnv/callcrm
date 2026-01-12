defmodule EduConsultCrm.Repo.Migrations.CreateLeadNotes do
  use Ecto.Migration

  def change do
    create table(:lead_notes, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :lead_id, references(:leads, type: :binary_id, on_delete: :delete_all), null: false
      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all)
      add :call_log_id, :binary_id

      add :content, :text, null: false
      add :note_type, :string, default: "general"
      add :is_pinned, :boolean, default: false

      timestamps(type: :utc_datetime)
    end

    create index(:lead_notes, [:lead_id])
    create index(:lead_notes, [:user_id])
    create index(:lead_notes, [:call_log_id])
    create index(:lead_notes, [:is_pinned])
  end
end
