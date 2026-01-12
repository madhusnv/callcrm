defmodule EduConsultCrm.Dashboard do
  @moduledoc """
  Aggregates dashboard statistics for an organization.
  """

  import Ecto.Query, warn: false
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Crm.Lead
  alias EduConsultCrm.Crm.LeadStatus
  alias EduConsultCrm.Calls.CallLog

  def get_stats(org_id) do
    today_start = DateTime.new!(Date.utc_today(), ~T[00:00:00], "Etc/UTC")
    tomorrow_start = DateTime.add(today_start, 86_400, :second)
    week_start_date = Date.beginning_of_week(Date.utc_today(), :monday)
    week_start = DateTime.new!(week_start_date, ~T[00:00:00], "Etc/UTC")

    total_leads =
      Lead
      |> where([l], l.organization_id == ^org_id and is_nil(l.deleted_at))
      |> select([l], count(l.id))
      |> Repo.one()

    new_leads_today =
      Lead
      |> where([l], l.organization_id == ^org_id and is_nil(l.deleted_at))
      |> where([l], l.inserted_at >= ^today_start and l.inserted_at < ^tomorrow_start)
      |> select([l], count(l.id))
      |> Repo.one()

    new_leads_this_week =
      Lead
      |> where([l], l.organization_id == ^org_id and is_nil(l.deleted_at))
      |> where([l], l.inserted_at >= ^week_start and l.inserted_at < ^tomorrow_start)
      |> select([l], count(l.id))
      |> Repo.one()

    pending_follow_ups =
      Lead
      |> where([l], l.organization_id == ^org_id and is_nil(l.deleted_at))
      |> where([l], not is_nil(l.next_follow_up_date))
      |> where(
        [l],
        l.next_follow_up_date >= ^today_start and l.next_follow_up_date < ^tomorrow_start
      )
      |> select([l], count(l.id))
      |> Repo.one()

    overdue_follow_ups =
      Lead
      |> where([l], l.organization_id == ^org_id and is_nil(l.deleted_at))
      |> where([l], not is_nil(l.next_follow_up_date))
      |> where([l], l.next_follow_up_date < ^today_start)
      |> select([l], count(l.id))
      |> Repo.one()

    {total_calls_today, total_call_duration_today} =
      CallLog
      |> where([c], c.organization_id == ^org_id)
      |> where([c], c.call_at >= ^today_start and c.call_at < ^tomorrow_start)
      |> select([c], {count(c.id), coalesce(sum(c.duration), 0)})
      |> Repo.one()

    %{
      totalLeads: total_leads || 0,
      newLeadsToday: new_leads_today || 0,
      newLeadsThisWeek: new_leads_this_week || 0,
      pendingFollowUps: pending_follow_ups || 0,
      overdueFollowUps: overdue_follow_ups || 0,
      totalCallsToday: total_calls_today || 0,
      totalCallDurationToday: total_call_duration_today || 0
    }
  end

  def leads_by_status(org_id) do
    LeadStatus
    |> where([s], s.organization_id == ^org_id and s.is_active == true)
    |> join(:left, [s], l in Lead,
      on:
        l.status_id == s.id and l.organization_id == ^org_id and
          is_nil(l.deleted_at)
    )
    |> group_by([s, _l], [s.id, s.name, s.color, s.order])
    |> order_by([s, _l], asc: s.order)
    |> select([s, l], %{
      id: s.id,
      name: s.name,
      color: s.color,
      order: s.order,
      count: count(l.id)
    })
    |> Repo.all()
  end

  def leads_by_source(org_id) do
    Lead
    |> where([l], l.organization_id == ^org_id and is_nil(l.deleted_at))
    |> where([l], not is_nil(l.source))
    |> group_by([l], l.source)
    |> order_by([_l], desc: count())
    |> select([l], %{source: l.source, count: count(l.id)})
    |> Repo.all()
  end

  def calls_by_type_today(org_id) do
    today_start = DateTime.new!(Date.utc_today(), ~T[00:00:00], "Etc/UTC")
    tomorrow_start = DateTime.add(today_start, 86_400, :second)

    CallLog
    |> where([c], c.organization_id == ^org_id)
    |> where([c], c.call_at >= ^today_start and c.call_at < ^tomorrow_start)
    |> group_by([c], c.call_type)
    |> select([c], %{type: c.call_type, count: count(c.id)})
    |> Repo.all()
  end
end
