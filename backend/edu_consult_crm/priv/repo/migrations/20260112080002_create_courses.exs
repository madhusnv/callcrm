defmodule EduConsultCrm.Repo.Migrations.CreateCourses do
  use Ecto.Migration

  def change do
    create table(:courses, primary_key: false) do
      add :id, :binary_id, primary_key: true

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :institution_id, references(:institutions, type: :binary_id, on_delete: :nilify_all)
      add :country_id, references(:countries, type: :binary_id, on_delete: :nilify_all)

      add :name, :string, null: false
      add :level, :string
      add :duration_months, :integer
      add :intake_months, {:array, :string}, default: []
      add :tuition_fee, :decimal
      add :currency_code, :string
      add :description, :text

      add :display_order, :integer, default: 0
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:courses, [:organization_id])
    create index(:courses, [:institution_id])
    create index(:courses, [:country_id])
    create index(:courses, [:is_active])
    create index(:courses, [:level])
    create unique_index(:courses, [:organization_id, :institution_id, :name])
  end
end
