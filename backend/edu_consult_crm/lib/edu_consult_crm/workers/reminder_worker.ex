defmodule EduConsultCrm.Workers.ReminderWorker do
  @moduledoc """
  Oban worker for sending follow-up reminders.
  Checks for leads with due follow-ups and sends notifications.
  """
  use Oban.Worker, queue: :reminders, max_attempts: 3

  alias EduConsultCrm.Crm
  alias EduConsultCrm.Accounts
  alias EduConsultCrm.Tenants
  alias EduConsultCrm.Notifications.FcmSender
  require Logger

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "check_due_followups", "org_id" => org_id}}) do
    Logger.info("Checking due follow-ups for org: #{org_id}")

    case Tenants.with_org(org_id, fn ->
           Accounts.list_users(org_id)
           |> Enum.map(fn user -> {user, Crm.count_due_callbacks(org_id, user.id)} end)
         end) do
      {:ok, user_counts} ->
        for {user, due_count} <- user_counts do
          if due_count > 0 do
            send_reminder_notification(user, due_count)
          end
        end

        :ok

      {:error, reason} ->
        Logger.error("Failed to check follow-ups for org #{org_id}: #{inspect(reason)}")
        {:error, reason}
    end
  end

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "check_due_followups_all"}}) do
    Tenants.list_organizations()
    |> Enum.each(fn org -> schedule_daily_check(org.id) end)

    :ok
  end

  @impl Oban.Worker
  def perform(%Oban.Job{
        args: %{"type" => "single_reminder", "user_id" => user_id, "lead_id" => lead_id} = args
      }) do
    Logger.info("Sending reminder for lead: #{lead_id} to user: #{user_id}")

    case resolve_org_for_reminder(Map.get(args, "org_id"), user_id, lead_id) do
      {:ok, org_id} ->
        case Tenants.with_org(org_id, fn ->
               {Accounts.get_user(user_id), Crm.get_lead(lead_id)}
             end) do
          {:ok, {nil, _}} ->
            Logger.warning("Reminder skipped: user #{user_id} not found")
            :ok

          {:ok, {_, nil}} ->
            Logger.warning("Reminder skipped: lead #{lead_id} not found")
            :ok

          {:ok, {user, lead}} ->
            if user.fcm_token do
              send_fcm_notification(user.fcm_token, %{
                title: "Follow-up Reminder",
                body: "Time to follow up with #{lead.first_name} #{lead.last_name || ""}",
                data: %{
                  type: "follow_up_reminder",
                  leadId: lead.id
                }
              })
            end

            :ok

          {:error, reason} ->
            Logger.error("Failed to load reminder data: #{inspect(reason)}")
            {:error, reason}
        end

      {:error, :not_found} ->
        Logger.warning("Reminder skipped: user or lead not found")
        :ok

      {:error, :org_mismatch} ->
        Logger.error("Reminder skipped: user and lead org mismatch")
        :ok

      {:error, reason} ->
        Logger.error("Failed to resolve reminder org: #{inspect(reason)}")
        {:error, reason}
    end
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
    FcmSender.send(fcm_token, payload)
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
    schedule_lead_reminder(nil, user_id, lead_id, scheduled_at)
  end

  def schedule_lead_reminder(org_id, user_id, lead_id, scheduled_at) do
    args = %{type: "single_reminder", user_id: user_id, lead_id: lead_id}
    args = if org_id, do: Map.put(args, :org_id, org_id), else: args

    args
    |> new(scheduled_at: scheduled_at)
    |> Oban.insert()
  end

  defp resolve_org_for_reminder(org_id, _user_id, _lead_id) when is_binary(org_id),
    do: {:ok, org_id}

  defp resolve_org_for_reminder(_, user_id, lead_id) do
    Tenants.with_bypass_rls(fn ->
      user = Accounts.get_user(user_id)
      lead = Crm.get_lead(lead_id)

      cond do
        is_nil(user) or is_nil(lead) ->
          {:error, :not_found}

        user.organization_id != lead.organization_id ->
          {:error, :org_mismatch}

        true ->
          {:ok, user.organization_id}
      end
    end)
    |> case do
      {:ok, {:ok, resolved_org_id}} -> {:ok, resolved_org_id}
      {:ok, {:error, reason}} -> {:error, reason}
      {:error, reason} -> {:error, reason}
    end
  end
end
