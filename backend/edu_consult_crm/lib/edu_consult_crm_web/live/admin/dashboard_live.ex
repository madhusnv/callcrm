defmodule EduConsultCrmWeb.Admin.DashboardLive do
  use EduConsultCrmWeb, :live_view

  alias EduConsultCrm.Dashboard
  alias EduConsultCrm.Crm
  alias EduConsultCrm.Tenants

  @impl Phoenix.LiveView
  def mount(params, _session, socket) do
    orgs = Tenants.list_organizations()
    org_id = params["org_id"] || (List.first(orgs) && List.first(orgs).id)

    socket =
      socket
      |> assign(:organizations, orgs)
      |> assign(:selected_org_id, org_id)

    {:ok, load_data(socket)}
  end

  @impl Phoenix.LiveView
  def handle_params(%{"org_id" => org_id}, _url, socket) do
    socket =
      socket
      |> assign(:selected_org_id, org_id)

    {:noreply, load_data(socket)}
  end

  def handle_params(_params, _url, socket), do: {:noreply, socket}

  defp load_data(socket) do
    case socket.assigns.selected_org_id do
      nil ->
        socket
        |> assign(:stats, nil)
        |> assign(:recent_leads, [])

      org_id ->
        stats = Dashboard.get_stats(org_id)

        leads =
          Crm.list_leads(org_id, %{
            page_size: 10,
            sort_order: :desc,
            preload: [:status]
          })

        socket
        |> assign(:stats, stats)
        |> assign(:recent_leads, leads)
    end
  end

  @impl Phoenix.LiveView
  def render(assigns) do
    ~H"""
    <Layouts.app flash={@flash}>
      <div class="mx-auto max-w-6xl px-6 py-8">
        <.admin_nav current="dashboard" />
        <div class="mt-6 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 class="text-2xl font-semibold">Admin Dashboard</h1>
            <p class="text-sm text-gray-500">Overview by organization</p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <%= for org <- @organizations do %>
              <.link
                patch={~p"/admin/dashboard?org_id=#{org.id}"}
                class={[
                  "rounded-full border px-3 py-1 text-sm",
                  if(org.id == @selected_org_id,
                    do: "bg-blue-600 text-white",
                    else: "text-gray-700"
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
            <.stat_card title="Total Leads" value={@stats.totalLeads} />
            <.stat_card title="New Today" value={@stats.newLeadsToday} />
            <.stat_card title="Pending Follow-ups" value={@stats.pendingFollowUps} />
            <.stat_card title="Overdue Follow-ups" value={@stats.overdueFollowUps} />
          </div>

          <div class="mt-8">
            <h2 class="text-lg font-semibold">Recent Leads</h2>
            <div class="mt-3 overflow-hidden rounded-lg border">
              <table class="min-w-full divide-y">
                <thead class="bg-gray-50 text-left text-xs font-medium uppercase text-gray-500">
                  <tr>
                    <th class="px-4 py-3">Name</th>
                    <th class="px-4 py-3">Phone</th>
                    <th class="px-4 py-3">Status</th>
                    <th class="px-4 py-3">Created</th>
                  </tr>
                </thead>
                <tbody class="divide-y bg-white text-sm text-gray-700">
                  <%= for lead <- @recent_leads do %>
                    <tr>
                      <td class="px-4 py-3">{lead.first_name} {lead.last_name}</td>
                      <td class="px-4 py-3">{lead.phone}</td>
                      <td class="px-4 py-3">{lead.status && lead.status.name}</td>
                      <td class="px-4 py-3">{lead.inserted_at}</td>
                    </tr>
                  <% end %>
                </tbody>
              </table>
            </div>
          </div>
        <% else %>
          <div class="mt-10 rounded-lg border border-dashed p-6 text-center text-gray-500">
            No organizations found.
          </div>
        <% end %>
      </div>
    </Layouts.app>
    """
  end

  defp stat_card(assigns) do
    ~H"""
    <div class="rounded-lg border bg-white p-4">
      <p class="text-sm text-gray-500">{@title}</p>
      <p class="mt-2 text-2xl font-semibold">{@value}</p>
    </div>
    """
  end

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
