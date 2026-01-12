defmodule EduConsultCrm.Crm do
  @moduledoc """
  The CRM context - handles leads, notes, activities, tags, and statuses.
  All queries are scoped by organization_id for multi-tenancy.
  """

  import Ecto.Query, warn: false
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Crm.{Lead, LeadNote, LeadActivity, LeadStatus, LeadTag}

  # =============================================================================
  # Leads
  # =============================================================================

  @doc """
  Lists leads for an organization with filtering, sorting, and pagination.

  ## Options
    * `:status_id` - filter by status ID
    * `:assigned_to` - filter by assigned user ID
    * `:priority` - filter by priority (low, medium, high, urgent)
    * `:source` - filter by source
    * `:branch_id` - filter by branch ID
    * `:tag_id` - filter by tag ID
    * `:date_from` / `:date_to` - filter by created_at range
    * `:follow_up_from` / `:follow_up_to` - filter by follow-up date range
    * `:follow_up_overdue` - filter overdue follow-ups (boolean)
    * `:search` - search in name, phone, email
    * `:include_deleted` - include soft-deleted leads
    * `:sort_by` - field to sort by (default: :inserted_at)
    * `:sort_order` - :asc or :desc (default: :desc)
    * `:page` - page number (default: 1)
    * `:page_size` - items per page (default: 20)
    * `:preload` - list of associations to preload
  """
  def list_leads(org_id, params \\ %{}) do
    page = Map.get(params, :page, 1)
    page_size = Map.get(params, :page_size, 20)
    offset = (page - 1) * page_size

    query =
      Lead
      |> where([l], l.organization_id == ^org_id)
      |> apply_lead_filters(params)
      |> apply_sorting(params)
      |> limit(^page_size)
      |> offset(^offset)

    preloads = Map.get(params, :preload, [:status, :assigned_user, :tags])

    query
    |> preload(^preloads)
    |> Repo.all()
  end

  @doc """
  Gets a lead by ID. Raises if not found.
  """
  def get_lead!(id), do: Repo.get!(Lead, id)

  @doc """
  Gets a lead by ID. Returns nil if not found.
  """
  def get_lead(id), do: Repo.get(Lead, id)

  @doc """
  Gets a lead by ID with preloads.
  """
  def get_lead(id, preloads) when is_list(preloads) do
    Lead
    |> Repo.get(id)
    |> Repo.preload(preloads)
  end

  @doc """
  Gets a lead by phone number within an organization.
  """
  def get_lead_by_phone(org_id, phone) do
    Lead
    |> where([l], l.organization_id == ^org_id and l.phone == ^phone)
    |> where([l], is_nil(l.deleted_at))
    |> Repo.one()
  end

  @doc """
  Creates a lead and logs the creation activity.
  """
  def create_lead(attrs, user) do
    Repo.transaction(fn ->
      with {:ok, lead} <- do_create_lead(attrs),
           {:ok, _activity} <- log_activity(lead, user, "created", "Lead created") do
        lead
      else
        {:error, changeset} -> Repo.rollback(changeset)
      end
    end)
  end

  defp do_create_lead(attrs) do
    %Lead{}
    |> Lead.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a lead and logs the update activity.
  """
  def update_lead(%Lead{} = lead, attrs, user) do
    Repo.transaction(fn ->
      changes = detect_changes(lead, attrs)

      with {:ok, updated_lead} <- do_update_lead(lead, attrs),
           {:ok, _activity} <- log_update_activity(updated_lead, user, changes) do
        updated_lead
      else
        {:error, changeset} -> Repo.rollback(changeset)
      end
    end)
  end

  defp do_update_lead(lead, attrs) do
    lead
    |> Lead.update_changeset(attrs)
    |> Repo.update()
  end

  defp detect_changes(lead, attrs) do
    attrs
    |> Enum.reduce(%{}, fn {key, new_value}, acc ->
      key_atom = if is_binary(key), do: String.to_existing_atom(key), else: key
      old_value = Map.get(lead, key_atom)

      if old_value != new_value do
        Map.put(acc, key_atom, %{from: old_value, to: new_value})
      else
        acc
      end
    end)
  rescue
    ArgumentError -> %{}
  end

  defp log_update_activity(lead, user, changes) when map_size(changes) > 0 do
    description =
      changes
      |> Map.keys()
      |> Enum.map(&to_string/1)
      |> Enum.join(", ")

    log_activity(lead, user, "updated", "Updated: #{description}", %{changes: changes})
  end

  defp log_update_activity(lead, user, _changes) do
    log_activity(lead, user, "updated", "Lead updated")
  end

  @doc """
  Soft deletes a lead.
  """
  def delete_lead(%Lead{} = lead, user) do
    Repo.transaction(fn ->
      with {:ok, deleted_lead} <- do_soft_delete(lead),
           {:ok, _activity} <- log_activity(deleted_lead, user, "deleted", "Lead deleted") do
        deleted_lead
      else
        {:error, changeset} -> Repo.rollback(changeset)
      end
    end)
  end

  defp do_soft_delete(lead) do
    lead
    |> Lead.soft_delete_changeset()
    |> Repo.update()
  end

  @doc """
  Restores a soft-deleted lead.
  """
  def restore_lead(%Lead{} = lead, user) do
    Repo.transaction(fn ->
      with {:ok, restored_lead} <- do_restore(lead),
           {:ok, _activity} <- log_activity(restored_lead, user, "restored", "Lead restored") do
        restored_lead
      else
        {:error, changeset} -> Repo.rollback(changeset)
      end
    end)
  end

  defp do_restore(lead) do
    lead
    |> Lead.restore_changeset()
    |> Repo.update()
  end

  @doc """
  Counts leads for an organization with optional filters.
  """
  def count_leads(org_id, params \\ %{}) do
    Lead
    |> where([l], l.organization_id == ^org_id)
    |> apply_lead_filters(params)
    |> Repo.aggregate(:count, :id)
  end

  @doc """
  Lists leads with total count for pagination.
  """
  def list_leads_with_count(org_id, params \\ %{}) do
    leads = list_leads(org_id, params)
    total = count_leads(org_id, params)
    {leads, total}
  end

  @doc """
  Gets a lead by ID for a specific organization.
  """
  def get_lead_for_org(org_id, id, opts \\ []) do
    include_deleted = Keyword.get(opts, :include_deleted, false)

    query =
      Lead
      |> where([l], l.id == ^id and l.organization_id == ^org_id)

    query =
      if include_deleted do
        query
      else
        where(query, [l], is_nil(l.deleted_at))
      end

    Repo.one(query)
  end

  @doc """
  Preloads associations for a lead.
  """
  def preload_lead(lead) do
    Repo.preload(lead, [:status, :assigned_user, :tags])
  end

  @doc """
  Counts due callbacks (overdue follow-ups) for a user.
  """
  def count_due_callbacks(org_id, user_id) do
    now = DateTime.utc_now()

    Lead
    |> where([l], l.organization_id == ^org_id)
    |> where([l], l.assigned_to == ^user_id)
    |> where([l], is_nil(l.deleted_at) and l.is_active == true)
    |> where([l], not is_nil(l.next_follow_up_date) and l.next_follow_up_date < ^now)
    |> Repo.aggregate(:count, :id)
  end

  # =============================================================================
  # Lead Statuses
  # =============================================================================

  @doc """
  Lists all active statuses for an organization, ordered by order field.
  """
  def list_statuses(org_id) do
    LeadStatus
    |> where([s], s.organization_id == ^org_id and s.is_active == true)
    |> order_by([s], asc: s.order, asc: s.name)
    |> Repo.all()
  end

  @doc """
  Gets the default status for an organization.
  """
  def get_default_status(org_id) do
    LeadStatus
    |> where([s], s.organization_id == ^org_id and s.is_default == true and s.is_active == true)
    |> Repo.one()
  end

  @doc """
  Gets a status by ID.
  """
  def get_status(id), do: Repo.get(LeadStatus, id)

  @doc """
  Gets a status by ID. Raises if not found.
  """
  def get_status!(id), do: Repo.get!(LeadStatus, id)

  @doc """
  Creates a status for an organization.
  """
  def create_status(attrs) do
    %LeadStatus{}
    |> LeadStatus.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a status.
  """
  def update_status(%LeadStatus{} = status, attrs) do
    status
    |> LeadStatus.changeset(attrs)
    |> Repo.update()
  end

  # =============================================================================
  # Lead Notes
  # =============================================================================

  @doc """
  Creates a note for a lead.
  """
  def create_note(%Lead{} = lead, user, params) do
    Repo.transaction(fn ->
      note_attrs = %{
        lead_id: lead.id,
        organization_id: lead.organization_id,
        user_id: user && user.id,
        content: params["note"] || params["content"],
        note_type: params["noteType"] || params["note_type"] || "general",
        is_pinned: params["isPinned"] || params["is_pinned"] || false,
        call_log_id: params["callLogId"] || params["call_log_id"]
      }

      with {:ok, note} <- do_create_note(note_attrs),
           {:ok, _lead} <- increment_notes_count(lead),
           {:ok, _activity} <- log_activity(lead, user, "note_added", "Note added") do
        Repo.preload(note, [:user])
      else
        {:error, changeset} -> Repo.rollback(changeset)
      end
    end)
  end

  defp do_create_note(attrs) do
    %LeadNote{}
    |> LeadNote.changeset(attrs)
    |> Repo.insert()
  end

  defp increment_notes_count(lead) do
    lead
    |> Ecto.Changeset.change(%{total_notes: lead.total_notes + 1})
    |> Repo.update()
  end

  @doc """
  Lists notes for a lead with pagination.
  """
  def list_notes(lead_id, params \\ %{}) do
    page = Map.get(params, :page, 1)
    page_size = Map.get(params, :page_size, 20)
    offset = (page - 1) * page_size

    LeadNote
    |> where([n], n.lead_id == ^lead_id)
    |> order_by([n], desc: n.is_pinned, desc: n.inserted_at)
    |> limit(^page_size)
    |> offset(^offset)
    |> preload([:user])
    |> Repo.all()
  end

  @doc """
  Lists notes with total count for pagination.
  """
  def list_notes_with_count(lead_id, params \\ %{}) do
    notes = list_notes(lead_id, params)
    total = count_notes(lead_id)
    {notes, total}
  end

  @doc """
  Counts notes for a lead.
  """
  def count_notes(lead_id) do
    LeadNote
    |> where([n], n.lead_id == ^lead_id)
    |> Repo.aggregate(:count, :id)
  end

  @doc """
  Gets a note by ID.
  """
  def get_note(id), do: Repo.get(LeadNote, id)

  @doc """
  Updates a note.
  """
  def update_note(%LeadNote{} = note, attrs) do
    note
    |> LeadNote.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a note.
  """
  def delete_note(%LeadNote{} = note) do
    Repo.delete(note)
  end

  # =============================================================================
  # Lead Tags
  # =============================================================================

  @doc """
  Lists all active tags for an organization.
  """
  def list_tags(org_id) do
    LeadTag
    |> where([t], t.organization_id == ^org_id and t.is_active == true)
    |> order_by([t], asc: t.name)
    |> Repo.all()
  end

  @doc """
  Gets a tag by ID.
  """
  def get_tag(id), do: Repo.get(LeadTag, id)

  @doc """
  Gets a tag by ID. Raises if not found.
  """
  def get_tag!(id), do: Repo.get!(LeadTag, id)

  @doc """
  Creates a tag for an organization.
  """
  def create_tag(attrs) do
    %LeadTag{}
    |> LeadTag.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a tag.
  """
  def update_tag(%LeadTag{} = tag, attrs) do
    tag
    |> LeadTag.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Adds a tag to a lead.
  """
  def add_tag(%Lead{} = lead, %LeadTag{} = tag) do
    if lead.organization_id != tag.organization_id do
      {:error, :forbidden}
    else
      Repo.insert_all(
        "lead_tags",
        [
          %{
            id: Ecto.UUID.generate(),
            organization_id: lead.organization_id,
            lead_id: lead.id,
            tag_id: tag.id,
            inserted_at: DateTime.utc_now() |> DateTime.truncate(:second),
            updated_at: DateTime.utc_now() |> DateTime.truncate(:second)
          }
        ],
        on_conflict: :nothing
      )

      {:ok, lead}
    end
  end

  @doc """
  Removes a tag from a lead.
  """
  def remove_tag(%Lead{} = lead, %LeadTag{} = tag) do
    from(
      lt in "lead_tags",
      where:
        lt.organization_id == ^lead.organization_id and
          lt.lead_id == ^lead.id and
          lt.tag_id == ^tag.id
    )
    |> Repo.delete_all()

    {:ok, lead}
  end

  # =============================================================================
  # Lead Activities
  # =============================================================================

  @doc """
  Logs an activity for a lead.
  """
  def log_activity(%Lead{} = lead, user, type, description, metadata \\ %{}) do
    attrs = %{
      lead_id: lead.id,
      organization_id: lead.organization_id,
      user_id: user && user.id,
      activity_type: type,
      description: description,
      metadata: metadata
    }

    %LeadActivity{}
    |> LeadActivity.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Lists activities for a lead with pagination.
  """
  def list_activities(lead_id, params \\ %{}) do
    page = Map.get(params, :page, 1)
    page_size = Map.get(params, :page_size, 20)
    offset = (page - 1) * page_size

    LeadActivity
    |> where([a], a.lead_id == ^lead_id)
    |> order_by([a], desc: a.inserted_at)
    |> limit(^page_size)
    |> offset(^offset)
    |> preload([:user])
    |> Repo.all()
  end

  # =============================================================================
  # Filtering Helpers
  # =============================================================================

  defp apply_lead_filters(query, params) do
    query
    |> filter_active(params)
    |> filter_by_status(params)
    |> filter_by_assigned(params)
    |> filter_by_priority(params)
    |> filter_by_source(params)
    |> filter_by_branch(params)
    |> filter_by_tag(params)
    |> filter_by_date_range(params)
    |> filter_by_follow_up(params)
    |> filter_by_search(params)
    |> filter_not_contacted(params)
  end

  defp filter_active(query, %{include_deleted: true}), do: query

  defp filter_active(query, _params) do
    where(query, [l], l.is_active == true and is_nil(l.deleted_at))
  end

  defp filter_by_status(query, %{status_id: status_id}) when not is_nil(status_id) do
    where(query, [l], l.status_id == ^status_id)
  end

  defp filter_by_status(query, _params), do: query

  defp filter_by_assigned(query, %{assigned_to: user_id}) when not is_nil(user_id) do
    if user_id == "unassigned" do
      where(query, [l], is_nil(l.assigned_to))
    else
      where(query, [l], l.assigned_to == ^user_id)
    end
  end

  defp filter_by_assigned(query, _params), do: query

  defp filter_by_priority(query, %{priority: priority}) when not is_nil(priority) do
    where(query, [l], l.priority == ^priority)
  end

  defp filter_by_priority(query, _params), do: query

  defp filter_by_source(query, %{source: source}) when not is_nil(source) do
    where(query, [l], l.source == ^source)
  end

  defp filter_by_source(query, _params), do: query

  defp filter_by_branch(query, %{branch_id: branch_id}) when not is_nil(branch_id) do
    where(query, [l], l.branch_id == ^branch_id)
  end

  defp filter_by_branch(query, _params), do: query

  defp filter_by_tag(query, %{tag_id: tag_id}) when not is_nil(tag_id) do
    query
    |> join(:inner, [l], lt in "lead_tags", on: lt.lead_id == l.id)
    |> where([l, lt], lt.tag_id == ^tag_id)
    |> distinct([l], l.id)
  end

  defp filter_by_tag(query, _params), do: query

  defp filter_by_date_range(query, params) do
    query
    |> maybe_filter_date_from(params)
    |> maybe_filter_date_to(params)
  end

  defp maybe_filter_date_from(query, %{date_from: date_from}) when not is_nil(date_from) do
    where(query, [l], l.inserted_at >= ^date_from)
  end

  defp maybe_filter_date_from(query, _params), do: query

  defp maybe_filter_date_to(query, %{date_to: date_to}) when not is_nil(date_to) do
    where(query, [l], l.inserted_at <= ^date_to)
  end

  defp maybe_filter_date_to(query, _params), do: query

  defp filter_by_follow_up(query, params) do
    query
    |> maybe_filter_follow_up_from(params)
    |> maybe_filter_follow_up_to(params)
    |> maybe_filter_follow_up_overdue(params)
  end

  defp maybe_filter_follow_up_from(query, %{follow_up_from: from}) when not is_nil(from) do
    where(query, [l], l.next_follow_up_date >= ^from)
  end

  defp maybe_filter_follow_up_from(query, _params), do: query

  defp maybe_filter_follow_up_to(query, %{follow_up_to: to}) when not is_nil(to) do
    where(query, [l], l.next_follow_up_date <= ^to)
  end

  defp maybe_filter_follow_up_to(query, _params), do: query

  defp maybe_filter_follow_up_overdue(query, %{follow_up_overdue: true}) do
    now = DateTime.utc_now()

    query
    |> where([l], not is_nil(l.next_follow_up_date) and l.next_follow_up_date < ^now)
  end

  defp maybe_filter_follow_up_overdue(query, _params), do: query

  defp filter_by_search(query, %{search: search}) when is_binary(search) and search != "" do
    search_term = "%#{search}%"

    where(
      query,
      [l],
      ilike(l.first_name, ^search_term) or
        ilike(l.last_name, ^search_term) or
        ilike(l.phone, ^search_term) or
        ilike(l.email, ^search_term) or
        ilike(l.student_name, ^search_term) or
        ilike(l.parent_name, ^search_term)
    )
  end

  defp filter_by_search(query, _params), do: query

  defp filter_not_contacted(query, %{not_contacted: true}) do
    where(query, [l], l.total_calls == 0 and is_nil(l.last_contact_date))
  end

  defp filter_not_contacted(query, _params), do: query

  # =============================================================================
  # Sorting
  # =============================================================================

  @allowed_sort_fields ~w(inserted_at updated_at first_name last_name priority next_follow_up_date last_contact_date)a

  defp apply_sorting(query, params) do
    sort_by = Map.get(params, :sort_by, :inserted_at)
    sort_order = Map.get(params, :sort_order, :desc)

    if sort_by in @allowed_sort_fields do
      order_by(query, [l], [{^sort_order, field(l, ^sort_by)}])
    else
      order_by(query, [l], desc: l.inserted_at)
    end
  end
end
