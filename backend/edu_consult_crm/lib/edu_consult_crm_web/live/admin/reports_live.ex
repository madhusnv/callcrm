defmodule EduConsultCrmWeb.Admin.ReportsLive do
  use EduConsultCrmWeb, :live_view

  alias EduConsultCrm.Dashboard
  alias EduConsultCrm.Tenants

  @impl Phoenix.LiveView
  def mount(params, _session, socket) do
    orgs = Tenants.list_organizations()
    org_id = params["org_id"] || (List.first(orgs) && List.first(orgs).id)

    socket =
      socket
      |> assign(:organizations, orgs)
      |> assign(:selected_org_id, org_id)

    {:ok, load_reports(socket)}
  end

  @impl Phoenix.LiveView
  def handle_params(%{"org_id" => org_id}, _url, socket) do
    socket =
      socket
      |> assign(:selected_org_id, org_id)

    {:noreply, load_reports(socket)}
  end

  def handle_params(_params, _url, socket), do: {:noreply, socket}

  defp load_reports(socket) do
    case socket.assigns.selected_org_id do
      nil ->
        socket
        |> assign(:stats, nil)
        |> assign(:status_breakdown, [])
        |> assign(:source_breakdown, [])
        |> assign(:call_breakdown, [])

      org_id ->
        stats = Dashboard.get_stats(org_id)
        status_breakdown = Dashboard.leads_by_status(org_id)
        source_breakdown = Dashboard.leads_by_source(org_id)
        call_breakdown = Dashboard.calls_by_type_today(org_id)

        socket
        |> assign(:stats, stats)
        |> assign(:status_breakdown, status_breakdown)
        |> assign(:source_breakdown, source_breakdown)
        |> assign(:call_breakdown, call_breakdown)
    end
  end

  @impl Phoenix.LiveView
  def render(assigns) do
    max_status =
      assigns.status_breakdown
      |> Enum.map(& &1.count)
      |> Enum.max(fn -> 0 end)

    max_source =
      assigns.source_breakdown
      |> Enum.map(& &1.count)
      |> Enum.max(fn -> 0 end)

    max_calls =
      assigns.call_breakdown
      |> Enum.map(& &1.count)
      |> Enum.max(fn -> 0 end)

    assigns =
      assigns
      |> assign(:max_status, max_status)
      |> assign(:max_source, max_source)
      |> assign(:max_calls, max_calls)

    ~H"""
    <Layouts.app flash={@flash}>
      <div class="min-h-screen bg-gradient-to-br from-slate-50 to-white">
        <div class="mx-auto max-w-6xl px-6 py-10">
          <.admin_nav current="reports" />
          <div class="mt-6 flex flex-wrap items-center justify-between gap-4">
            <div>
              <h1 class="text-3xl font-semibold text-slate-900">Reports</h1>
              <p class="text-sm text-slate-500">Operational view by organization</p>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <%= for org <- @organizations do %>
                <.link
                  patch={~p"/admin/reports?org_id=#{org.id}"}
                  class={[
                    "rounded-full border px-3 py-1 text-sm transition",
                    if(org.id == @selected_org_id,
                      do: "bg-slate-900 text-white",
                      else: "text-slate-700 hover:bg-slate-100"
                    )
                  ]}
                >
                  {org.name}
                </.link>
              <% end %>
            </div>
          </div>

          <%= if @stats do %>
            <div class="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <.stat title="Total Leads" value={@stats.totalLeads} />
              <.stat title="New This Week" value={@stats.newLeadsThisWeek} />
              <.stat title="Pending Follow-ups" value={@stats.pendingFollowUps} />
              <.stat title="Overdue Follow-ups" value={@stats.overdueFollowUps} />
            </div>

            <div class="mt-10 grid grid-cols-1 gap-6 lg:grid-cols-2">
              <.bar_card
                title="Lead Status Mix"
                rows={@status_breakdown}
                max={@max_status}
                value_key={:count}
                label_key={:name}
              />
              <.bar_card
                title="Lead Sources"
                rows={@source_breakdown}
                max={@max_source}
                value_key={:count}
                label_key={:source}
              />
            </div>

            <div class="mt-10">
              <.bar_card
                title="Today's Calls"
                rows={@call_breakdown}
                max={@max_calls}
                value_key={:count}
                label_key={:type}
              />
            </div>
          <% else %>
            <div class="mt-10 rounded-xl border border-dashed p-8 text-center text-slate-500">
              No data available yet.
            </div>
          <% end %>
        </div>
      </div>
    </Layouts.app>
    """
  end

  defp stat(assigns) do
    ~H"""
    <div class="rounded-xl border bg-white p-5 shadow-sm">
      <p class="text-xs uppercase tracking-wide text-slate-400">{@title}</p>
      <p class="mt-3 text-2xl font-semibold text-slate-900">{@value}</p>
    </div>
    """
  end

  defp bar_card(assigns) do
    ~H"""
    <div class="rounded-xl border bg-white p-6 shadow-sm">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold text-slate-900">{@title}</h2>
        <span class="text-xs text-slate-400">count</span>
      </div>
      <div class="mt-5 space-y-3">
        <%= for row <- @rows do %>
          <div>
            <div class="flex items-center justify-between text-sm">
              <span class="font-medium text-slate-700">{Map.get(row, @label_key)}</span>
              <span class="text-slate-500">{Map.get(row, @value_key)}</span>
            </div>
            <div class="mt-2 h-2 w-full rounded-full bg-slate-100">
              <div
                class="h-2 rounded-full bg-slate-900"
                style={"width: #{bar_width(Map.get(row, @value_key), @max)}%"}
              >
              </div>
            </div>
          </div>
        <% end %>
        <%= if Enum.empty?(@rows) do %>
          <p class="text-sm text-slate-400">No data to display.</p>
        <% end %>
      </div>
    </div>
    """
  end

  defp bar_width(count, max) when max > 0 do
    Float.round(count * 100 / max, 0)
  end

  defp bar_width(_count, _max), do: 0

  defp admin_nav(assigns) do
    ~H"""
    <nav class="flex flex-wrap items-center gap-3 rounded-full border bg-white px-4 py-2 text-sm">
      <.nav_link label="Dashboard" to={~p"/admin/dashboard"} active={@current == "dashboard"} />
      <.nav_link label="Leads" to={~p"/admin/leads"} active={@current == "leads"} />
      <.nav_link label="Reports" to={~p"/admin/reports"} active={@current == "reports"} />
    </nav>
    """
  end

  defp nav_link(assigns) do
    ~H"""
    <.link
      navigate={@to}
      class={[
        "rounded-full px-3 py-1 transition",
        if(@active,
          do: "bg-slate-900 text-white",
          else: "text-slate-700 hover:bg-slate-100"
        )
      ]}
    >
      {@label}
    </.link>
    """
  end
end
