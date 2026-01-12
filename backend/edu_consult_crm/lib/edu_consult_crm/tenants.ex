defmodule EduConsultCrm.Tenants do
  @moduledoc """
  The Tenants context for multi-tenancy.
  """

  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Tenants.{Organization, Branch}

  @doc """
  Gets organization by API key. Used by TenantPlug.
  """
  def get_organization_by_api_key(api_key) when is_binary(api_key) do
    Organization
    |> where([o], o.api_key == ^api_key and o.is_active == true)
    |> Repo.one()
    |> case do
      nil ->
        {:error, :invalid_api_key}

      %{subscription_status: "suspended"} = org ->
        {:error, {:suspended, org.suspension_reason}}

      %{subscription_status: "expired"} ->
        {:error, :subscription_expired}

      org ->
        {:ok, org}
    end
  end

  def get_organization_by_api_key(_), do: {:error, :invalid_api_key}

  @doc """
  Gets organization by slug.
  """
  def get_organization_by_slug(slug) do
    Repo.get_by(Organization, slug: slug, is_active: true)
  end

  @doc """
  Gets a single organization.
  """
  def get_organization!(id), do: Repo.get!(Organization, id)

  @doc """
  Lists all organizations.
  """
  def list_organizations do
    Repo.all(Organization)
  end

  @doc """
  Creates an organization.
  """
  def create_organization(attrs \\ %{}) do
    %Organization{}
    |> Organization.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Updates an organization.
  """
  def update_organization(%Organization{} = org, attrs) do
    org
    |> Organization.changeset(attrs)
    |> Repo.update()
  end

  @doc """
  Creates a branch.
  """
  def create_branch(attrs \\ %{}) do
    %Branch{}
    |> Branch.changeset(attrs)
    |> Repo.insert()
  end

  @doc """
  Lists branches for an organization.
  """
  def list_branches(org_id) do
    Branch
    |> where([b], b.organization_id == ^org_id and b.is_active == true)
    |> Repo.all()
  end

  @doc """
  Checks if organization has a feature enabled.
  """
  def has_feature?(%Organization{features: features}, feature) do
    feature in features
  end

  @doc """
  Checks if organization has reached user limit.
  """
  def user_limit_reached?(%Organization{current_users_count: count, max_users: max}) do
    count >= max
  end

  @doc """
  Checks if organization has reached lead limit.
  """
  def lead_limit_reached?(%Organization{current_leads_count: count, max_leads: max}) do
    count >= max
  end

  @doc """
  Increments user count.
  """
  def increment_user_count(%Organization{} = org) do
    org
    |> Ecto.Changeset.change(%{current_users_count: org.current_users_count + 1})
    |> Repo.update()
  end
end
