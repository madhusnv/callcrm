defmodule EduConsultCrm.Repo.Migrations.CreateBranches do
  use Ecto.Migration

  def change do
    create table(:branches, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :name, :string, null: false
      add :code, :string
      add :address, :text
      add :phone, :string
      add :email, :string
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:branches, [:organization_id])
    create unique_index(:branches, [:organization_id, :code])
  end
end
