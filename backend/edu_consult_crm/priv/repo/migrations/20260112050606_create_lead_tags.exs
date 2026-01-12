defmodule EduConsultCrm.Repo.Migrations.CreateLeadTags do
  use Ecto.Migration

  def change do
    create table(:tags, primary_key: false) do
      add :id, :binary_id, primary_key: true

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :name, :string, null: false
      add :color, :string, default: "#2196F3"
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:tags, [:organization_id])
    create unique_index(:tags, [:organization_id, :name])

    create table(:lead_tags, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :lead_id, references(:leads, type: :binary_id, on_delete: :delete_all), null: false
      add :tag_id, references(:tags, type: :binary_id, on_delete: :delete_all), null: false

      timestamps(type: :utc_datetime)
    end

    create index(:lead_tags, [:lead_id])
    create index(:lead_tags, [:tag_id])
    create unique_index(:lead_tags, [:lead_id, :tag_id])
  end
end
