defmodule EduConsultCrm.Education do
  @moduledoc """
  The Education context - handles countries, institutions, and courses.
  All queries are scoped by organization_id for multi-tenancy.
  """

  import Ecto.Query, warn: false
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Education.{Country, Institution, Course}

  # =============================================================================
  # Countries
  # =============================================================================

  def list_countries(org_id, params \\ %{}) do
    Country
    |> where([c], c.organization_id == ^org_id)
    |> filter_active(params)
    |> search_by_name(params)
    |> order_by([c], asc: c.display_order, asc: c.name)
    |> Repo.all()
  end

  def get_country(org_id, id) do
    Country
    |> where([c], c.organization_id == ^org_id and c.id == ^id)
    |> Repo.one()
  end

  def create_country(org_id, attrs) do
    %Country{organization_id: org_id}
    |> Country.changeset(attrs)
    |> Repo.insert()
  end

  def update_country(%Country{} = country, attrs) do
    country
    |> Country.changeset(attrs)
    |> Repo.update()
  end

  # =============================================================================
  # Institutions
  # =============================================================================

  def list_institutions(org_id, params \\ %{}) do
    Institution
    |> where([i], i.organization_id == ^org_id)
    |> filter_active(params)
    |> filter_by_country(params)
    |> search_by_name(params)
    |> order_by([i], asc: i.display_order, asc: i.name)
    |> maybe_preload(params, [])
    |> Repo.all()
  end

  def get_institution(org_id, id) do
    Institution
    |> where([i], i.organization_id == ^org_id and i.id == ^id)
    |> Repo.one()
  end

  def create_institution(org_id, attrs) do
    %Institution{organization_id: org_id}
    |> Institution.changeset(attrs)
    |> Repo.insert()
  end

  def update_institution(%Institution{} = institution, attrs) do
    institution
    |> Institution.changeset(attrs)
    |> Repo.update()
  end

  # =============================================================================
  # Courses
  # =============================================================================

  def list_courses(org_id, params \\ %{}) do
    Course
    |> where([c], c.organization_id == ^org_id)
    |> filter_active(params)
    |> filter_by_country(params)
    |> filter_by_institution(params)
    |> filter_by_level(params)
    |> search_by_name(params)
    |> order_by([c], asc: c.display_order, asc: c.name)
    |> maybe_preload(params, [])
    |> Repo.all()
  end

  def get_course(org_id, id) do
    Course
    |> where([c], c.organization_id == ^org_id and c.id == ^id)
    |> Repo.one()
  end

  def create_course(org_id, attrs) do
    %Course{organization_id: org_id}
    |> Course.changeset(attrs)
    |> Repo.insert()
  end

  def update_course(%Course{} = course, attrs) do
    course
    |> Course.changeset(attrs)
    |> Repo.update()
  end

  # =============================================================================
  # Private helpers
  # =============================================================================

  defp filter_active(query, %{include_inactive: true}), do: query
  defp filter_active(query, _), do: where(query, [q], q.is_active == true)

  defp filter_by_country(query, %{country_id: country_id}) when not is_nil(country_id) do
    where(query, [q], q.country_id == ^country_id)
  end

  defp filter_by_country(query, _), do: query

  defp filter_by_institution(query, %{institution_id: institution_id})
       when not is_nil(institution_id) do
    where(query, [q], q.institution_id == ^institution_id)
  end

  defp filter_by_institution(query, _), do: query

  defp filter_by_level(query, %{level: level}) when not is_nil(level) do
    where(query, [q], q.level == ^level)
  end

  defp filter_by_level(query, _), do: query

  defp search_by_name(query, %{search: search})
       when is_binary(search) and byte_size(search) > 0 do
    where(query, [q], ilike(q.name, ^"%#{search}%"))
  end

  defp search_by_name(query, _), do: query

  defp maybe_preload(query, %{preload: preloads}, _default) when is_list(preloads) do
    preload(query, ^preloads)
  end

  defp maybe_preload(query, _params, default), do: preload(query, ^default)
end
