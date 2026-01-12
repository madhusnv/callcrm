defmodule EduConsultCrm.Repo.Migrations.CreateInstitutions do
  use Ecto.Migration

  def change do
    create table(:institutions, primary_key: false) do
      add :id, :binary_id, primary_key: true

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :country_id, references(:countries, type: :binary_id, on_delete: :nilify_all)

      add :name, :string, null: false
      add :city, :string
      add :institution_type, :string
      add :website, :string
      add :logo_url, :string

      add :display_order, :integer, default: 0
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:institutions, [:organization_id])
    create index(:institutions, [:country_id])
    create index(:institutions, [:is_active])
    create unique_index(:institutions, [:organization_id, :name])
  end
end
