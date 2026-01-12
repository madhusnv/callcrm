defmodule EduConsultCrm.Workers.ReminderWorker do
  @moduledoc """
  Oban worker for sending follow-up reminders.
  Checks for leads with due follow-ups and sends notifications.
  """
  use Oban.Worker, queue: :reminders, max_attempts: 3

  alias EduConsultCrm.Crm
  alias EduConsultCrm.Accounts
  require Logger

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "check_due_followups", "org_id" => org_id}}) do
    Logger.info("Checking due follow-ups for org: #{org_id}")

    # Get all users in the organization
    users = Accounts.list_users(org_id)

    for user <- users do
      due_count = Crm.count_due_callbacks(org_id, user.id)

      if due_count > 0 do
        send_reminder_notification(user, due_count)
      end
    end

    :ok
  end

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "single_reminder", "user_id" => user_id, "lead_id" => lead_id}}) do
    Logger.info("Sending reminder for lead: #{lead_id} to user: #{user_id}")

    user = Accounts.get_user(user_id)
    lead = Crm.get_lead!(lead_id)

    if user && user.fcm_token do
      send_fcm_notification(user.fcm_token, %{
        title: "Follow-up Reminder",
        body: "Time to follow up with #{lead.first_name} #{lead.last_name || ""}",
        data: %{
          type: "follow_up_reminder",
          lead_id: lead.id
        }
      })
    end

    :ok
  end

  defp send_reminder_notification(user, count) do
    if user.fcm_token do
      send_fcm_notification(user.fcm_token, %{
        title: "Follow-up Reminders",
        body: "You have #{count} follow-up(s) due today",
        data: %{
          type: "daily_reminder",
          count: count
        }
      })
    end
  end

  defp send_fcm_notification(fcm_token, payload) do
    # TODO: Implement FCM sending using Pigeon or similar
    # For now, just log
    Logger.info("Would send FCM to #{fcm_token}: #{inspect(payload)}")
    :ok
  end

  @doc """
  Schedules a check for due follow-ups.
  Call this from a daily cron job or scheduler.
  """
  def schedule_daily_check(org_id) do
    %{type: "check_due_followups", org_id: org_id}
    |> new()
    |> Oban.insert()
  end

  @doc """
  Schedules a single reminder for a specific lead.
  """
  def schedule_lead_reminder(user_id, lead_id, scheduled_at) do
    %{type: "single_reminder", user_id: user_id, lead_id: lead_id}
    |> new(scheduled_at: scheduled_at)
    |> Oban.insert()
  end
end
