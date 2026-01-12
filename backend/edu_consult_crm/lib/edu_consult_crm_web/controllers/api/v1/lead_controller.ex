defmodule EduConsultCrmWeb.Api.V1.LeadController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Crm
  alias EduConsultCrm.Accounts.Guardian

  action_fallback EduConsultCrmWeb.FallbackController

  # POST /lead/getData
  def index(conn, params) do
    org = conn.assigns[:current_org]

    {leads, total} = Crm.list_leads_with_count(org.id, normalize_params(params))
    page = Map.get(params, "page", 1)

    render(conn, :index, leads: leads, total: total, page: page)
  end

  # POST /lead/save
  def create_or_update(conn, %{"leadDetails" => lead_params}) do
    org = conn.assigns[:current_org]
    user = Guardian.Plug.current_resource(conn)

    result =
      case lead_params["id"] do
        nil ->
          attrs = normalize_lead_params(lead_params, org.id, user.id)
          Crm.create_lead(attrs, user)

        id ->
          case Crm.get_lead_for_org(org.id, id) do
            nil ->
              {:error, :not_found}

            lead ->
              attrs = normalize_lead_params(lead_params, org.id, nil)
              Crm.update_lead(lead, attrs, user)
          end
      end

    case result do
      {:ok, lead} ->
        lead = Crm.preload_lead(lead)
        render(conn, :show, lead: lead)

      {:error, :not_found} ->
        conn
        |> put_status(:not_found)
        |> render(:error, message: "Lead not found")

      {:error, :leads_limit_reached} ->
        conn
        |> put_status(:payment_required)
        |> render(:error, message: "Lead limit reached. Please upgrade your plan.")

      {:error, changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> render(:error, changeset: changeset)
    end
  end

  # POST /lead/saveNote
  def save_note(conn, %{"leadId" => lead_id} = params) do
    org = conn.assigns[:current_org]
    user = Guardian.Plug.current_resource(conn)

    with {:ok, lead} <- get_lead_for_org(org.id, lead_id),
         {:ok, note} <- Crm.create_note(lead, user, params) do
      render(conn, :note, note: note)
    end
  end

  # POST /lead/status
  def list_statuses(conn, _params) do
    org = conn.assigns[:current_org]
    statuses = Crm.list_statuses(org.id)

    render(conn, :statuses, statuses: statuses)
  end

  # POST /lead/getByNumber
  def get_by_number(conn, %{"phone" => phone}) do
    org = conn.assigns[:current_org]

    case Crm.get_lead_by_phone(org.id, phone) do
      nil ->
        conn
        |> put_status(:ok)
        |> render(:show, lead: nil)

      lead ->
        lead = Crm.preload_lead(lead)
        render(conn, :show, lead: lead)
    end
  end

  # POST /lead/note
  def get_notes(conn, %{"leadId" => lead_id} = params) do
    org = conn.assigns[:current_org]

    with {:ok, _lead} <- get_lead_for_org(org.id, lead_id) do
      {notes, total} = Crm.list_notes_with_count(lead_id, normalize_params(params))
      page = Map.get(params, "page", 1)

      render(conn, :notes, notes: notes, total: total, page: page)
    end
  end

  # POST /lead/allTags
  def all_tags(conn, _params) do
    org = conn.assigns[:current_org]
    tags = Crm.list_tags(org.id)

    render(conn, :tags, tags: tags)
  end

  # POST /lead/notContacted
  def not_contacted(conn, params) do
    org = conn.assigns[:current_org]
    user = Guardian.Plug.current_resource(conn)

    filter_params =
      params
      |> normalize_params()
      |> Map.put(:not_contacted, true)
      |> maybe_filter_by_user(user, params)

    {leads, total} = Crm.list_leads_with_count(org.id, filter_params)
    page = Map.get(params, "page", 1)

    render(conn, :index, leads: leads, total: total, page: page)
  end

  # POST /lead/isDeleted
  def check_deleted(conn, %{"leadId" => id}) do
    org = conn.assigns[:current_org]

    case Crm.get_lead_for_org(org.id, id, include_deleted: true) do
      nil ->
        conn
        |> put_status(:not_found)
        |> render(:error, message: "Lead not found")

      lead ->
        render(conn, :deleted_status, lead: lead)
    end
  end

  # POST /lead/restore
  def restore(conn, %{"leadId" => id}) do
    org = conn.assigns[:current_org]
    user = Guardian.Plug.current_resource(conn)

    with {:ok, lead} <- get_lead_for_org(org.id, id, include_deleted: true),
         {:ok, restored_lead} <- Crm.restore_lead(lead, user) do
      restored_lead = Crm.preload_lead(restored_lead)
      render(conn, :show, lead: restored_lead)
    end
  end

  # POST /lead/callBack/totalDue
  def due_callbacks(conn, _params) do
    org = conn.assigns[:current_org]
    user = Guardian.Plug.current_resource(conn)

    count = Crm.count_due_callbacks(org.id, user.id)

    render(conn, :due_count, count: count)
  end

  # Private helpers

  defp get_lead_for_org(org_id, lead_id, opts \\ []) do
    case Crm.get_lead_for_org(org_id, lead_id, opts) do
      nil -> {:error, :not_found}
      lead -> {:ok, lead}
    end
  end

  defp normalize_params(params) do
    %{
      page: parse_int(params["page"], 1),
      page_size: parse_int(params["pageSize"], 20),
      status_id: params["statusId"],
      assigned_to: params["assignedTo"],
      priority: params["priority"],
      source: params["source"],
      branch_id: params["branchId"],
      tag_id: params["tagId"],
      search: params["search"],
      sort_by: parse_sort_by(params["sortBy"]),
      sort_order: parse_sort_order(params["sortOrder"]),
      date_from: parse_datetime(params["dateFrom"]),
      date_to: parse_datetime(params["dateTo"]),
      follow_up_from: parse_datetime(params["followUpFrom"]),
      follow_up_to: parse_datetime(params["followUpTo"]),
      follow_up_overdue: params["followUpOverdue"] == true
    }
    |> Enum.reject(fn {_k, v} -> is_nil(v) end)
    |> Map.new()
  end

  defp normalize_lead_params(params, org_id, created_by) do
    base = %{
      "organization_id" => org_id,
      "first_name" => params["firstName"],
      "last_name" => params["lastName"],
      "phone" => params["phone"],
      "secondary_phone" => params["secondaryPhone"],
      "country_code" => params["countryCode"],
      "email" => params["email"],
      "student_name" => params["studentName"],
      "parent_name" => params["parentName"],
      "relationship" => params["relationship"],
      "date_of_birth" => params["dateOfBirth"],
      "current_education" => params["currentEducation"],
      "current_institution" => params["currentInstitution"],
      "percentage" => params["percentage"],
      "stream" => params["stream"],
      "graduation_year" => params["graduationYear"],
      "interested_courses" => params["interestedCourses"],
      "preferred_countries" => params["preferredCountries"],
      "preferred_institutions" => params["preferredInstitutions"],
      "budget_min" => params["budgetMin"],
      "budget_max" => params["budgetMax"],
      "intake_preference" => params["intakePreference"],
      "priority" => params["priority"],
      "source" => params["source"],
      "status_id" => params["statusId"],
      "branch_id" => params["branchId"],
      "assigned_to" => params["assignedTo"],
      "next_follow_up_date" => params["nextFollowUpDate"],
      "reminder_note" => params["reminderNote"],
      "custom_fields" => params["customFields"]
    }

    base =
      if created_by do
        Map.put(base, "created_by", created_by)
      else
        base
      end

    base
    |> Enum.reject(fn {_k, v} -> is_nil(v) end)
    |> Map.new()
  end

  defp maybe_filter_by_user(params, user, request_params) do
    if request_params["myLeads"] == true do
      Map.put(params, :assigned_to, user.id)
    else
      params
    end
  end

  defp parse_int(nil, default), do: default
  defp parse_int(val, _default) when is_integer(val), do: val

  defp parse_int(val, default) when is_binary(val) do
    case Integer.parse(val) do
      {int, _} -> int
      :error -> default
    end
  end

  defp parse_sort_by(nil), do: nil
  defp parse_sort_by("createdAt"), do: :inserted_at
  defp parse_sort_by("updatedAt"), do: :updated_at
  defp parse_sort_by("firstName"), do: :first_name
  defp parse_sort_by("lastName"), do: :last_name
  defp parse_sort_by("priority"), do: :priority
  defp parse_sort_by("nextFollowUpDate"), do: :next_follow_up_date
  defp parse_sort_by("lastContactDate"), do: :last_contact_date
  defp parse_sort_by(_), do: nil

  defp parse_sort_order(nil), do: nil
  defp parse_sort_order("asc"), do: :asc
  defp parse_sort_order("desc"), do: :desc
  defp parse_sort_order(_), do: nil

  defp parse_datetime(nil), do: nil

  defp parse_datetime(val) when is_binary(val) do
    case DateTime.from_iso8601(val) do
      {:ok, datetime, _} -> datetime
      _ -> nil
    end
  end
end
