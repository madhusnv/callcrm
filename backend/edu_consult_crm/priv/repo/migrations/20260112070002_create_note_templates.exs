defmodule EduConsultCrm.Repo.Migrations.CreateNoteTemplates do
  use Ecto.Migration

  def change do
    create table(:note_templates, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      
      add :name, :string, null: false
      add :content, :text, null: false
      add :category, :string             # call, follow_up, general, status_change
      add :shortcut, :string             # Quick access key like "noa" for "No Answer"
      add :order, :integer, default: 0
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:note_templates, [:organization_id])
    create index(:note_templates, [:category])
    create index(:note_templates, [:is_active])
  end
end
