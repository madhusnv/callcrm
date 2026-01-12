defmodule EduConsultCrm.Repo.Migrations.CreateUsers do
  use Ecto.Migration

  def change do
    create table(:users, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :branch_id, references(:branches, type: :binary_id, on_delete: :nilify_all)

      # Auth
      add :email, :string, null: false
      add :phone, :string, null: false
      add :password_hash, :string, null: false

      # Profile
      add :first_name, :string, null: false
      add :last_name, :string
      add :role, :string, null: false, default: "agent"
      add :avatar_url, :string

      # Push Notifications
      add :fcm_token, :string
      add :device_info, :map

      # Status
      add :last_login_at, :utc_datetime
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create unique_index(:users, [:email, :organization_id])
    create unique_index(:users, [:phone, :organization_id])
    create index(:users, [:organization_id])
    create index(:users, [:branch_id])
    create index(:users, [:role])
  end
end
