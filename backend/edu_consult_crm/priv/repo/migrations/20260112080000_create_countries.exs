defmodule EduConsultCrm.Repo.Migrations.CreateCountries do
  use Ecto.Migration

  def change do
    create table(:countries, primary_key: false) do
      add :id, :binary_id, primary_key: true

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :name, :string, null: false
      add :code, :string, null: false
      add :currency_code, :string
      add :phone_code, :string

      add :display_order, :integer, default: 0
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:countries, [:organization_id])
    create index(:countries, [:is_active])
    create unique_index(:countries, [:organization_id, :code])
    create unique_index(:countries, [:organization_id, :name])
  end
end
