defmodule EduConsultCrmWeb.Api.V1.LeadJSON do
  alias EduConsultCrm.Crm.{Lead, LeadStatus, LeadNote, LeadTag}
  alias EduConsultCrm.Accounts.User

  def index(%{leads: leads, total: total, page: page}) do
    %{
      status: true,
      data: %{
        leads: for(lead <- leads, do: data(lead)),
        total: total,
        page: page
      }
    }
  end

  def show(%{lead: nil}) do
    %{status: true, data: nil}
  end

  def show(%{lead: lead}) do
    %{status: true, data: data(lead)}
  end

  def note(%{note: note}) do
    %{status: true, data: note_data(note)}
  end

  def statuses(%{statuses: statuses}) do
    %{status: true, data: for(s <- statuses, do: status_data(s))}
  end

  def notes(%{notes: notes, total: total, page: page}) do
    %{
      status: true,
      data: %{
        notes: for(n <- notes, do: note_data(n)),
        total: total,
        page: page
      }
    }
  end

  def tags(%{tags: tags}) do
    %{status: true, data: for(t <- tags, do: tag_data(t))}
  end

  def deleted_status(%{lead: lead}) do
    %{
      status: true,
      data: %{
        id: lead.id,
        isDeleted: not is_nil(lead.deleted_at),
        deletedAt: lead.deleted_at
      }
    }
  end

  def due_count(%{count: count}) do
    %{status: true, data: %{count: count}}
  end

  def error(%{message: message}) do
    %{status: false, message: message}
  end

  def error(%{changeset: changeset}) do
    %{status: false, message: "Validation failed", errors: format_errors(changeset)}
  end

  defp data(%Lead{} = lead) do
    %{
      id: lead.id,
      firstName: lead.first_name,
      lastName: lead.last_name,
      phone: lead.phone,
      secondaryPhone: lead.secondary_phone,
      countryCode: lead.country_code,
      email: lead.email,
      studentName: lead.student_name,
      parentName: lead.parent_name,
      relationship: lead.relationship,
      dateOfBirth: lead.date_of_birth,
      currentEducation: lead.current_education,
      currentInstitution: lead.current_institution,
      percentage: lead.percentage && Decimal.to_float(lead.percentage),
      stream: lead.stream,
      graduationYear: lead.graduation_year,
      interestedCourses: lead.interested_courses,
      preferredCountries: lead.preferred_countries,
      preferredInstitutions: lead.preferred_institutions,
      budgetMin: lead.budget_min && Decimal.to_float(lead.budget_min),
      budgetMax: lead.budget_max && Decimal.to_float(lead.budget_max),
      intakePreference: lead.intake_preference,
      priority: lead.priority,
      source: lead.source,
      lastContactDate: lead.last_contact_date,
      nextFollowUpDate: lead.next_follow_up_date,
      reminderNote: lead.reminder_note,
      totalCalls: lead.total_calls,
      totalNotes: lead.total_notes,
      customFields: lead.custom_fields,
      isActive: lead.is_active,
      deletedAt: lead.deleted_at,
      statusId: lead.status_id,
      status: lead.status && status_data(lead.status),
      branchId: lead.branch_id,
      assignedTo: lead.assigned_to,
      assignedUser: lead.assigned_user && user_data(lead.assigned_user),
      createdBy: lead.created_by,
      tags: (lead.tags || []) |> Enum.map(&tag_data/1),
      createdAt: lead.inserted_at,
      updatedAt: lead.updated_at
    }
  end

  defp status_data(%LeadStatus{} = status) do
    %{
      id: status.id,
      name: status.name,
      code: status.code,
      color: status.color,
      order: status.order,
      isDefault: status.is_default,
      isClosed: status.is_closed
    }
  end

  defp note_data(%LeadNote{} = note) do
    %{
      id: note.id,
      leadId: note.lead_id,
      content: note.content,
      noteType: note.note_type,
      isPinned: note.is_pinned,
      callLogId: note.call_log_id,
      userId: note.user_id,
      user: note.user && user_data(note.user),
      createdAt: note.inserted_at,
      updatedAt: note.updated_at
    }
  end

  defp tag_data(%LeadTag{} = tag) do
    %{
      id: tag.id,
      name: tag.name,
      color: tag.color
    }
  end

  defp user_data(%User{} = user) do
    %{
      id: user.id,
      firstName: user.first_name,
      lastName: user.last_name,
      email: user.email,
      phone: user.phone,
      role: user.role
    }
  end

  defp format_errors(changeset) do
    Ecto.Changeset.traverse_errors(changeset, fn {msg, opts} ->
      Enum.reduce(opts, msg, fn {key, value}, acc ->
        String.replace(acc, "%{#{key}}", to_string(value))
      end)
    end)
  end
end
