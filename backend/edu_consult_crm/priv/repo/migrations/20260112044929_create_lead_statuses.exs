defmodule EduConsultCrm.Repo.Migrations.CreateLeadStatuses do
  use Ecto.Migration

  def change do
    create table(:lead_statuses, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :name, :string, null: false
      add :code, :string, null: false
      add :color, :string, default: "#2196F3"
      add :order, :integer, default: 0
      add :is_default, :boolean, default: false
      add :is_closed, :boolean, default: false
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create unique_index(:lead_statuses, [:organization_id, :code])
    create index(:lead_statuses, [:organization_id])
  end
end
