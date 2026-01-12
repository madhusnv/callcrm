defmodule EduConsultCrm.Repo.Migrations.CreateOrganizations do
  use Ecto.Migration

  def change do
    create table(:organizations, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :name, :string, null: false
      add :slug, :string, null: false

      # API Keys for multi-tenancy
      add :api_key, :string, null: false
      add :api_secret, :string
      add :api_key_generated_at, :utc_datetime

      # Contact Info
      add :email, :string
      add :phone, :string
      add :address, :text
      add :logo_url, :string

      # Settings
      add :settings, :map, default: %{}
      add :features, {:array, :string}, default: []

      # Subscription
      add :plan, :string, default: "free"
      add :max_users, :integer, default: 5
      add :max_leads, :integer, default: 500
      add :subscription_status, :string, default: "active"
      add :subscription_expires_at, :utc_datetime

      # Usage Tracking
      add :current_users_count, :integer, default: 0
      add :current_leads_count, :integer, default: 0
      add :storage_used_bytes, :bigint, default: 0
      add :storage_limit_bytes, :bigint, default: 1_073_741_824

      # Status
      add :is_active, :boolean, default: true
      add :suspended_at, :utc_datetime
      add :suspension_reason, :string

      timestamps(type: :utc_datetime)
    end

    create unique_index(:organizations, [:slug])
    create unique_index(:organizations, [:api_key])
    create index(:organizations, [:api_key, :is_active])
  end
end
