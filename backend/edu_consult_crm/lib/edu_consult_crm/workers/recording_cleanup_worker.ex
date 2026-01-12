defmodule EduConsultCrm.Workers.RecordingCleanupWorker do
  @moduledoc """
  Oban worker for cleaning up expired call recordings from S3.
  Runs periodically to remove recordings past their retention period.
  """
  use Oban.Worker, queue: :cleanup, max_attempts: 3

  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Calls.CallRecording
  require Logger

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "cleanup_expired"}}) do
    Logger.info("Starting recording cleanup job")

    now = DateTime.utc_now()

    # Find expired recordings
    expired_recordings =
      CallRecording
      |> where([r], r.expires_at < ^now)
      |> where([r], r.is_deleted == false)
      |> where([r], r.status == "uploaded")
      |> Repo.all()

    Logger.info("Found #{length(expired_recordings)} expired recordings")

    # Delete each from S3 and mark as deleted
    results = Enum.map(expired_recordings, &cleanup_recording/1)

    deleted_count = Enum.count(results, fn r -> r == :ok end)
    failed_count = Enum.count(results, fn r -> r != :ok end)

    Logger.info("Cleanup complete: #{deleted_count} deleted, #{failed_count} failed")

    :ok
  end

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "cleanup_failed"}}) do
    Logger.info("Starting failed recordings cleanup")

    # Find recordings that failed upload multiple times
    failed_recordings =
      CallRecording
      |> where([r], r.status == "failed")
      |> where([r], r.retry_count >= 3)
      |> where([r], r.is_deleted == false)
      |> Repo.all()

    # Just mark them as deleted (no S3 file to delete)
    for recording <- failed_recordings do
      recording
      |> Ecto.Changeset.change(%{is_deleted: true})
      |> Repo.update()
    end

    Logger.info("Marked #{length(failed_recordings)} failed recordings as deleted")

    :ok
  end

  defp cleanup_recording(%CallRecording{} = recording) do
    # Delete from S3
    case delete_from_s3(recording.storage_bucket, recording.storage_key) do
      :ok ->
        # Mark as deleted in database
        recording
        |> Ecto.Changeset.change(%{is_deleted: true})
        |> Repo.update()

        Logger.debug("Deleted recording #{recording.id}")
        :ok

      {:error, reason} ->
        Logger.error("Failed to delete recording #{recording.id}: #{inspect(reason)}")
        {:error, reason}
    end
  end

  defp delete_from_s3(bucket, key) when is_binary(bucket) and is_binary(key) do
    bucket
    |> ExAws.S3.delete_object(key)
    |> ExAws.request()
    |> case do
      {:ok, _} -> :ok
      {:error, reason} -> {:error, reason}
    end
  rescue
    e -> {:error, e}
  end

  defp delete_from_s3(_, _), do: :ok

  @doc """
  Schedules the cleanup job to run.
  Call this from application startup or a scheduler.
  """
  def schedule_cleanup do
    %{type: "cleanup_expired"}
    |> new()
    |> Oban.insert()
  end

  @doc """
  Schedules cleanup of failed recordings.
  """
  def schedule_failed_cleanup do
    %{type: "cleanup_failed"}
    |> new()
    |> Oban.insert()
  end
end
