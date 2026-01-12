defmodule EduConsultCrm.Calls.CallRecording do
  @moduledoc """
  Schema for call recordings uploaded to S3.
  """
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @statuses ~w(pending uploading uploaded failed)

  schema "call_recordings" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization
    belongs_to :call_log, EduConsultCrm.Calls.CallLog
    belongs_to :user, EduConsultCrm.Accounts.User

    # Recording metadata
    field :original_file_name, :string
    field :original_file_size, :integer
    field :compressed_file_size, :integer
    field :duration, :integer
    field :format, :string, default: "mp3"
    field :bitrate, :integer

    # S3 Storage
    field :storage_key, :string
    field :storage_url, :string
    field :storage_bucket, :string

    # Status tracking
    field :status, :string, default: "pending"
    field :upload_started_at, :utc_datetime
    field :upload_completed_at, :utc_datetime
    field :retry_count, :integer, default: 0
    field :last_error, :string

    # Retention
    field :expires_at, :utc_datetime
    field :is_deleted, :boolean, default: false

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(organization_id call_log_id user_id)a
  @optional_fields ~w(
    original_file_name original_file_size compressed_file_size duration format bitrate
    storage_key storage_url storage_bucket
    status upload_started_at upload_completed_at retry_count last_error
    expires_at is_deleted
  )a

  def changeset(recording, attrs) do
    recording
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_inclusion(:status, @statuses)
    |> foreign_key_constraint(:organization_id)
    |> foreign_key_constraint(:call_log_id)
    |> foreign_key_constraint(:user_id)
    |> unique_constraint([:call_log_id], name: :call_recordings_call_log_unique)
  end

  def upload_changeset(recording, attrs) do
    recording
    |> cast(attrs, [:storage_key, :storage_url, :storage_bucket, :status, :upload_started_at])
    |> validate_required([:storage_key, :storage_bucket])
    |> put_change(:status, "uploading")
    |> put_change(:upload_started_at, DateTime.utc_now() |> DateTime.truncate(:second))
  end

  def confirm_changeset(recording, attrs) do
    recording
    |> cast(attrs, [:compressed_file_size, :duration, :format, :bitrate])
    |> put_change(:status, "uploaded")
    |> put_change(:upload_completed_at, DateTime.utc_now() |> DateTime.truncate(:second))
  end

  def fail_changeset(recording, error) do
    recording
    |> change(%{
      status: "failed",
      last_error: error,
      retry_count: (recording.retry_count || 0) + 1
    })
  end

  def statuses, do: @statuses
end
