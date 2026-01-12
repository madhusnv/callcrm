defmodule EduConsultCrm.Templates do
  @moduledoc """
  The Templates context - handles note and message templates.
  """

  import Ecto.Query, warn: false
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Templates.{NoteTemplate, MessageTemplate}

  # =============================================================================
  # Note Templates
  # =============================================================================

  @doc """
  Lists all active note templates for an organization.
  """
  def list_note_templates(org_id, params \\ %{}) do
    NoteTemplate
    |> where([t], t.organization_id == ^org_id)
    |> filter_active(params)
    |> filter_by_category(params, :category)
    |> order_by([t], [asc: t.order, asc: t.name])
    |> Repo.all()
  end

  @doc """
  Gets a note template by ID.
  """
  def get_note_template(org_id, id) do
    NoteTemplate
    |> where([t], t.organization_id == ^org_id and t.id == ^id)
    |> Repo.one()
  end

  def get_note_template!(org_id, id) do
    NoteTemplate
    |> where([t], t.organization_id == ^org_id and t.id == ^id)
    |> Repo.one!()
  end

  @doc """
  Creates a note template.
  """
  def create_note_template(org_id, attrs) do
    %NoteTemplate{organization_id: org_id}
    |> NoteTemplate.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a note template.
  """
  def update_note_template(%NoteTemplate{} = template, attrs) do
    template
    |> NoteTemplate.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a note template (soft delete by setting is_active = false).
  """
  def delete_note_template(%NoteTemplate{} = template) do
    template
    |> Ecto.Changeset.change(%{is_active: false})
    |> Repo.update()
  end

  # =============================================================================
  # Message Templates
  # =============================================================================

  @doc """
  Lists all active message templates for an organization.
  """
  def list_message_templates(org_id, params \\ %{}) do
    MessageTemplate
    |> where([t], t.organization_id == ^org_id)
    |> filter_active(params)
    |> filter_by_category(params, :category)
    |> filter_by_channel(params)
    |> order_by([t], [asc: t.order, asc: t.name])
    |> Repo.all()
  end

  @doc """
  Gets a message template by ID.
  """
  def get_message_template(org_id, id) do
    MessageTemplate
    |> where([t], t.organization_id == ^org_id and t.id == ^id)
    |> Repo.one()
  end

  def get_message_template!(org_id, id) do
    MessageTemplate
    |> where([t], t.organization_id == ^org_id and t.id == ^id)
    |> Repo.one!()
  end

  @doc """
  Creates a message template.
  """
  def create_message_template(org_id, attrs) do
    %MessageTemplate{organization_id: org_id}
    |> MessageTemplate.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates a message template.
  """
  def update_message_template(%MessageTemplate{} = template, attrs) do
    template
    |> MessageTemplate.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Deletes a message template (soft delete).
  """
  def delete_message_template(%MessageTemplate{} = template) do
    template
    |> Ecto.Changeset.change(%{is_active: false})
    |> Repo.update()
  end

  @doc """
  Renders a message template with dynamic field substitution.

  ## Example

      render_message_template(template, %{"name" => "John", "course" => "MBA"})
      # Returns: "Hello John, we have an update about your MBA application..."
  """
  def render_message_template(%MessageTemplate{content: content}, values) when is_map(values) do
    Enum.reduce(values, content, fn {key, value}, acc ->
      String.replace(acc, "{{#{key}}}", to_string(value))
    end)
  end

  # =============================================================================
  # Private Helpers
  # =============================================================================

  defp filter_active(query, %{include_inactive: true}), do: query
  defp filter_active(query, _), do: where(query, [t], t.is_active == true)

  defp filter_by_category(query, %{category: category}, _) when not is_nil(category) do
    where(query, [t], t.category == ^category)
  end
  defp filter_by_category(query, _, _), do: query

  defp filter_by_channel(query, %{channel: "whatsapp"}) do
    where(query, [t], t.whatsapp_enabled == true)
  end
  defp filter_by_channel(query, %{channel: "sms"}) do
    where(query, [t], t.sms_enabled == true)
  end
  defp filter_by_channel(query, _), do: query
end
