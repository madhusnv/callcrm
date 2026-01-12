defmodule EduConsultCrmWeb.Admin.LeadLive.Index do
  use EduConsultCrmWeb, :live_view

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
      |> assign(:leads, [])

    {:ok, load_leads(socket)}
  end

  @impl Phoenix.LiveView
  def handle_params(%{"org_id" => org_id}, _url, socket) do
    socket =
      socket
      |> assign(:selected_org_id, org_id)

    {:noreply, load_leads(socket)}
  end

  def handle_params(_params, _url, socket), do: {:noreply, socket}

  defp load_leads(socket) do
    case socket.assigns.selected_org_id do
      nil ->
        assign(socket, :leads, [])

      org_id ->
        leads =
          Crm.list_leads(org_id, %{
            page_size: 50,
            sort_order: :desc,
            preload: [:status]
          })

        assign(socket, :leads, leads)
    end
  end

  @impl Phoenix.LiveView
  def render(assigns) do
    ~H"""
    <Layouts.app flash={@flash}>
      <div class="mx-auto max-w-6xl px-6 py-8">
        <.admin_nav current="leads" />
        <div class="mt-6 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 class="text-2xl font-semibold">Leads</h1>
            <p class="text-sm text-gray-500">Manage leads by organization</p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <%= for org <- @organizations do %>
              <.link
                patch={~p"/admin/leads?org_id=#{org.id}"}
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

        <div class="mt-6 overflow-hidden rounded-lg border">
          <table class="min-w-full divide-y">
            <thead class="bg-gray-50 text-left text-xs font-medium uppercase text-gray-500">
              <tr>
                <th class="px-4 py-3">Name</th>
                <th class="px-4 py-3">Phone</th>
                <th class="px-4 py-3">Status</th>
                <th class="px-4 py-3">Follow-up</th>
                <th class="px-4 py-3">Created</th>
              </tr>
            </thead>
            <tbody class="divide-y bg-white text-sm text-gray-700">
              <%= for lead <- @leads do %>
                <tr>
                  <td class="px-4 py-3">{lead.first_name} {lead.last_name}</td>
                  <td class="px-4 py-3">{lead.phone}</td>
                  <td class="px-4 py-3">{lead.status && lead.status.name}</td>
                  <td class="px-4 py-3">{lead.next_follow_up_date || "—"}</td>
                  <td class="px-4 py-3">{lead.inserted_at}</td>
                </tr>
              <% end %>
            </tbody>
          </table>
        </div>
      </div>
    </Layouts.app>
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
