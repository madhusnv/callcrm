defmodule EduConsultCrmWeb.Api.V1.EducationController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Education
  alias EduConsultCrm.Tenants

  action_fallback EduConsultCrmWeb.FallbackController

  @doc """
  POST /api/v1/education/countries
  Lists countries for the organization.
  """
  def list_countries(conn, params) do
    org = conn.assigns.current_org

    filter_params = %{
      include_inactive: parse_bool(params["includeInactive"]),
      search: params["search"]
    }

    case Tenants.with_org(org.id, fn -> Education.list_countries(org.id, filter_params) end) do
      {:ok, countries} ->
        conn
        |> put_status(:ok)
        |> json(%{status: true, data: Enum.map(countries, &country_data/1)})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to fetch countries"})
    end
  end

  @doc """
  POST /api/v1/education/institutions
  Lists institutions for the organization.
  """
  def list_institutions(conn, params) do
    org = conn.assigns.current_org

    filter_params = %{
      include_inactive: parse_bool(params["includeInactive"]),
      country_id: params["countryId"],
      search: params["search"]
    }

    case Tenants.with_org(org.id, fn -> Education.list_institutions(org.id, filter_params) end) do
      {:ok, institutions} ->
        conn
        |> put_status(:ok)
        |> json(%{status: true, data: Enum.map(institutions, &institution_data/1)})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to fetch institutions"})
    end
  end

  @doc """
  POST /api/v1/education/courses
  Lists courses for the organization.
  """
  def list_courses(conn, params) do
    org = conn.assigns.current_org

    filter_params = %{
      include_inactive: parse_bool(params["includeInactive"]),
      country_id: params["countryId"],
      institution_id: params["institutionId"],
      level: params["level"],
      search: params["search"]
    }

    case Tenants.with_org(org.id, fn -> Education.list_courses(org.id, filter_params) end) do
      {:ok, courses} ->
        conn
        |> put_status(:ok)
        |> json(%{status: true, data: Enum.map(courses, &course_data/1)})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to fetch courses"})
    end
  end

  @doc """
  POST /api/v1/education/course/get
  Gets a course by id for the organization.
  """
  def get_course(conn, %{"courseId" => course_id}) do
    org = conn.assigns.current_org

    case Tenants.with_org(org.id, fn -> Education.get_course(org.id, course_id) end) do
      {:ok, nil} ->
        {:error, :not_found}

      {:ok, course} ->
        conn
        |> put_status(:ok)
        |> json(%{status: true, data: course_data(course)})

      {:error, _} ->
        conn
        |> put_status(:internal_server_error)
        |> json(%{status: false, message: "Failed to fetch course"})
    end
  end

  # Private helpers

  defp parse_bool(true), do: true
  defp parse_bool(false), do: false
  defp parse_bool("true"), do: true
  defp parse_bool("false"), do: false
  defp parse_bool(_), do: false

  defp country_data(country) do
    %{
      id: country.id,
      name: country.name,
      code: country.code,
      currencyCode: country.currency_code,
      phoneCode: country.phone_code,
      displayOrder: country.display_order,
      isActive: country.is_active
    }
  end

  defp institution_data(institution) do
    %{
      id: institution.id,
      countryId: institution.country_id,
      name: institution.name,
      city: institution.city,
      institutionType: institution.institution_type,
      website: institution.website,
      logoUrl: institution.logo_url,
      displayOrder: institution.display_order,
      isActive: institution.is_active
    }
  end

  defp course_data(course) do
    %{
      id: course.id,
      countryId: course.country_id,
      institutionId: course.institution_id,
      name: course.name,
      level: course.level,
      durationMonths: course.duration_months,
      intakeMonths: course.intake_months,
      tuitionFee: course.tuition_fee,
      currencyCode: course.currency_code,
      description: course.description,
      displayOrder: course.display_order,
      isActive: course.is_active
    }
  end
end
