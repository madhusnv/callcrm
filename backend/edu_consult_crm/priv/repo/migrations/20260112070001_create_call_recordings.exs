defmodule EduConsultCrm.Repo.Migrations.CreateCallRecordings do
  use Ecto.Migration

  def change do
    create table(:call_recordings, primary_key: false) do
      add :id, :binary_id, primary_key: true

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :call_log_id, references(:call_logs, type: :binary_id, on_delete: :delete_all),
        null: false

      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all), null: false

      # Recording metadata
      add :original_file_name, :string
      # Size in bytes
      add :original_file_size, :bigint
      # Size after FFmpeg compression
      add :compressed_file_size, :bigint
      # Duration in seconds
      add :duration, :integer
      # mp3, m4a, etc.
      add :format, :string, default: "mp3"
      # Bitrate in kbps
      add :bitrate, :integer

      # S3 Storage
      # S3 object key
      add :storage_key, :string
      # Public or presigned URL
      add :storage_url, :string
      # Bucket name
      add :storage_bucket, :string

      # Status tracking
      # pending, uploading, uploaded, failed
      add :status, :string, default: "pending"
      add :upload_started_at, :utc_datetime
      add :upload_completed_at, :utc_datetime
      add :retry_count, :integer, default: 0
      add :last_error, :text

      # Retention
      # For auto-cleanup
      add :expires_at, :utc_datetime
      add :is_deleted, :boolean, default: false

      timestamps(type: :utc_datetime)
    end

    create index(:call_recordings, [:organization_id])
    create index(:call_recordings, [:call_log_id])
    create index(:call_recordings, [:user_id])
    create index(:call_recordings, [:status])
    create index(:call_recordings, [:expires_at])

    create unique_index(:call_recordings, [:call_log_id],
             where: "is_deleted = false",
             name: :call_recordings_call_log_unique
           )
  end
end
