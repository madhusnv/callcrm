defmodule EduConsultCrm.Repo.Migrations.CreateCallLogs do
  use Ecto.Migration

  def change do
    create table(:call_logs, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all), null: false
      add :lead_id, references(:leads, type: :binary_id, on_delete: :nilify_all)

      add :phone_number, :string, null: false
      add :call_type, :string, null: false  # incoming, outgoing, missed
      add :duration, :integer, default: 0   # seconds
      add :call_at, :utc_datetime, null: false
      add :sim_slot, :integer
      add :device_call_id, :string          # Unique ID from device call log
      add :contact_name, :string            # Name from device contacts
      add :notes, :text

      timestamps(type: :utc_datetime)
    end

    create index(:call_logs, [:organization_id])
    create index(:call_logs, [:user_id])
    create index(:call_logs, [:lead_id])
    create index(:call_logs, [:phone_number])
    create index(:call_logs, [:call_at])
    create index(:call_logs, [:call_type])
    create unique_index(:call_logs, [:user_id, :device_call_id], name: :call_logs_user_device_unique)
  end
end
