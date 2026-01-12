defmodule EduConsultCrmWeb.Api.V1.TemplateController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Templates

  action_fallback EduConsultCrmWeb.FallbackController

  @doc """
  POST /api/v1/callnote/template/fetchAll
  Lists all note templates for the organization.
  """
  def list_note_templates(conn, params) do
    org = conn.assigns.current_org
    
    filter_params = %{
      category: params["category"]
    }

    templates = Templates.list_note_templates(org.id, filter_params)

    conn
    |> put_status(:ok)
    |> json(%{
      status: true,
      data: Enum.map(templates, &note_template_data/1)
    })
  end

  @doc """
  POST /api/v1/messagetemplate/fetchAll
  Lists all message templates for the organization.
  """
  def list_message_templates(conn, params) do
    org = conn.assigns.current_org
    
    filter_params = %{
      category: params["category"],
      channel: params["channel"]  # "whatsapp" or "sms"
    }

    templates = Templates.list_message_templates(org.id, filter_params)

    conn
    |> put_status(:ok)
    |> json(%{
      status: true,
      data: Enum.map(templates, &message_template_data/1)
    })
  end

  @doc """
  POST /api/v1/messagetemplate/render
  Renders a message template with provided values.

  Request body:
  {
    "template_id": "uuid",
    "values": {"name": "John", "course": "MBA"}
  }
  """
  def render_template(conn, %{"template_id" => template_id, "values" => values}) do
    org = conn.assigns.current_org

    case Templates.get_message_template(org.id, template_id) do
      nil ->
        conn
        |> put_status(:not_found)
        |> json(%{status: false, message: "Template not found"})

      template ->
        rendered = Templates.render_message_template(template, values)

        conn
        |> put_status(:ok)
        |> json(%{
          status: true,
          data: %{
            rendered_content: rendered,
            template_name: template.name
          }
        })
    end
  end

  # Private helpers

  defp note_template_data(template) do
    %{
      id: template.id,
      name: template.name,
      content: template.content,
      category: template.category,
      shortcut: template.shortcut,
      order: template.order
    }
  end

  defp message_template_data(template) do
    %{
      id: template.id,
      name: template.name,
      content: template.content,
      category: template.category,
      dynamic_fields: template.dynamic_fields,
      whatsapp_enabled: template.whatsapp_enabled,
      sms_enabled: template.sms_enabled,
      order: template.order
    }
  end
end
