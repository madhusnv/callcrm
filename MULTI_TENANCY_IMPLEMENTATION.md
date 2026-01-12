# Multi-Tenancy Implementation Guide
## API Key Based Tenant Resolution - Elixir/Phoenix

---

## Overview

| Aspect | Approach |
|--------|----------|
| **Tenant Resolution** | API Key in Header (`X-API-Key`) |
| **Data Isolation** | Row-level (`organization_id` column) |
| **Query Scoping** | Automatic via Plug + Repo callbacks |
| **Tenant Context** | Process dictionary + Conn assigns |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Mobile App Request                      │
│                                                             │
│  Headers:                                                   │
│    Authorization: Bearer <jwt_token>                        │
│    X-API-Key: org_abc123xyz                                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Phoenix Pipeline                         │
│                                                             │
│  1. TenantPlug       → Resolve org from API key             │
│  2. AuthPipeline     → Verify JWT, load user                │
│  3. TenantScopePlug  → Verify user belongs to tenant        │
│  4. Controller       → Process request                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repo with Tenant Scope                    │
│                                                             │
│  - All queries auto-filtered by organization_id            │
│  - Inserts auto-tagged with organization_id                │
│  - Cross-tenant access prevented                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. Database Schema

### Organizations Table with API Keys

```elixir
# priv/repo/migrations/001_create_organizations.exs
defmodule EduConsultCrm.Repo.Migrations.CreateOrganizations do
  use Ecto.Migration

  def change do
    create table(:organizations, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :name, :string, null: false
      add :slug, :string, null: false
      
      # API Keys
      add :api_key, :string, null: false
      add :api_secret, :string  # Optional: for signed requests
      add :api_key_generated_at, :utc_datetime
      
      # Settings
      add :email, :string
      add :phone, :string
      add :address, :text
      add :logo_url, :string
      add :settings, :map, default: %{}
      add :features, {:array, :string}, default: []  # Enabled features
      
      # Subscription
      add :plan, :string, default: "free"  # free, starter, pro, enterprise
      add :max_users, :integer, default: 5
      add :max_leads, :integer, default: 500
      add :subscription_status, :string, default: "active"
      add :subscription_expires_at, :utc_datetime
      
      # Limits & Usage
      add :current_users_count, :integer, default: 0
      add :current_leads_count, :integer, default: 0
      add :storage_used_bytes, :bigint, default: 0
      add :storage_limit_bytes, :bigint, default: 1_073_741_824  # 1GB
      
      add :is_active, :boolean, default: true
      add :suspended_at, :utc_datetime
      add :suspension_reason, :string

      timestamps(type: :utc_datetime)
    end

    create unique_index(:organizations, [:slug])
    create unique_index(:organizations, [:api_key])
    
    # Index for fast API key lookup
    create index(:organizations, [:api_key, :is_active])
  end
end
```

### API Key History (Audit Trail)

```elixir
# priv/repo/migrations/002_create_api_key_history.exs
defmodule EduConsultCrm.Repo.Migrations.CreateApiKeyHistory do
  use Ecto.Migration

  def change do
    create table(:api_key_history, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :api_key, :string, null: false
      add :action, :string, null: false  # created, rotated, revoked
      add :rotated_by, references(:users, type: :binary_id, on_delete: :nilify_all)
      add :ip_address, :string
      add :user_agent, :string
      add :revoked_at, :utc_datetime

      timestamps(type: :utc_datetime, updated_at: false)
    end

    create index(:api_key_history, [:organization_id])
    create index(:api_key_history, [:api_key])
  end
end
```

---

## 2. Organization Schema & Context

```elixir
# lib/edu_consult_crm/tenants/organization.ex
defmodule EduConsultCrm.Tenants.Organization do
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  schema "organizations" do
    field :name, :string
    field :slug, :string
    field :api_key, :string
    field :api_secret, :string
    field :api_key_generated_at, :utc_datetime
    
    field :email, :string
    field :phone, :string
    field :address, :string
    field :logo_url, :string
    field :settings, :map, default: %{}
    field :features, {:array, :string}, default: []
    
    field :plan, :string, default: "free"
    field :max_users, :integer, default: 5
    field :max_leads, :integer, default: 500
    field :subscription_status, :string, default: "active"
    field :subscription_expires_at, :utc_datetime
    
    field :current_users_count, :integer, default: 0
    field :current_leads_count, :integer, default: 0
    field :storage_used_bytes, :integer, default: 0
    field :storage_limit_bytes, :integer, default: 1_073_741_824
    
    field :is_active, :boolean, default: true
    field :suspended_at, :utc_datetime
    field :suspension_reason, :string

    has_many :users, EduConsultCrm.Accounts.User
    has_many :branches, EduConsultCrm.Tenants.Branch
    has_many :leads, EduConsultCrm.Crm.Lead
    has_many :lead_statuses, EduConsultCrm.Crm.LeadStatus

    timestamps(type: :utc_datetime)
  end

  def changeset(org, attrs) do
    org
    |> cast(attrs, [:name, :slug, :email, :phone, :address, :logo_url, :settings, :features, :plan])
    |> validate_required([:name, :slug])
    |> unique_constraint(:slug)
    |> unique_constraint(:api_key)
    |> maybe_generate_api_key()
  end

  defp maybe_generate_api_key(changeset) do
    if get_field(changeset, :api_key) do
      changeset
    else
      changeset
      |> put_change(:api_key, generate_api_key())
      |> put_change(:api_key_generated_at, DateTime.utc_now())
    end
  end

  def generate_api_key do
    "org_" <> Base.encode32(:crypto.strong_rand_bytes(20), case: :lower, padding: false)
  end

  def generate_api_secret do
    Base.encode64(:crypto.strong_rand_bytes(32))
  end
end
```

### Tenants Context

```elixir
# lib/edu_consult_crm/tenants/tenants.ex
defmodule EduConsultCrm.Tenants do
  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Tenants.{Organization, ApiKeyHistory}

  # ============================================
  # API Key Resolution (Called by TenantPlug)
  # ============================================
  
  @doc """
  Resolves organization from API key.
  Returns {:ok, organization} or {:error, reason}
  """
  def get_organization_by_api_key(api_key) when is_binary(api_key) do
    Organization
    |> where([o], o.api_key == ^api_key and o.is_active == true)
    |> Repo.one()
    |> case do
      nil -> {:error, :invalid_api_key}
      %{subscription_status: "suspended"} = org -> {:error, {:suspended, org.suspension_reason}}
      %{subscription_status: "expired"} -> {:error, :subscription_expired}
      org -> {:ok, org}
    end
  end

  def get_organization_by_api_key(_), do: {:error, :invalid_api_key}

  # ============================================
  # Organization CRUD
  # ============================================

  def create_organization(attrs) do
    %Organization{}
    |> Organization.changeset(attrs)
    |> Repo.insert()
    |> case do
      {:ok, org} ->
        log_api_key_action(org.id, org.api_key, "created")
        {:ok, org}
      error -> error
    end
  end

  def get_organization!(id), do: Repo.get!(Organization, id)

  def update_organization(%Organization{} = org, attrs) do
    org
    |> Organization.changeset(attrs)
    |> Repo.update()
  end

  # ============================================
  # API Key Management
  # ============================================

  @doc """
  Rotates API key for an organization.
  Old key is immediately invalidated.
  """
  def rotate_api_key(org_id, rotated_by_user_id \\ nil) do
    org = get_organization!(org_id)
    old_key = org.api_key
    new_key = Organization.generate_api_key()

    org
    |> Ecto.Changeset.change(%{
      api_key: new_key,
      api_key_generated_at: DateTime.utc_now()
    })
    |> Repo.update()
    |> case do
      {:ok, updated_org} ->
        # Log old key as revoked
        log_api_key_action(org_id, old_key, "revoked", rotated_by_user_id)
        # Log new key as created
        log_api_key_action(org_id, new_key, "rotated", rotated_by_user_id)
        
        # Invalidate cached org (if using cache)
        invalidate_org_cache(old_key)
        
        {:ok, updated_org}
      error -> error
    end
  end

  defp log_api_key_action(org_id, api_key, action, user_id \\ nil) do
    %ApiKeyHistory{}
    |> ApiKeyHistory.changeset(%{
      organization_id: org_id,
      api_key: mask_api_key(api_key),
      action: action,
      rotated_by: user_id,
      revoked_at: if(action == "revoked", do: DateTime.utc_now())
    })
    |> Repo.insert()
  end

  defp mask_api_key(key) do
    # Store only first 8 and last 4 chars for audit
    prefix = String.slice(key, 0, 8)
    suffix = String.slice(key, -4, 4)
    "#{prefix}...#{suffix}"
  end

  defp invalidate_org_cache(api_key) do
    # If using Cachex or similar
    Cachex.del(:org_cache, api_key)
  end

  # ============================================
  # Usage Tracking
  # ============================================

  def increment_leads_count(org_id) do
    from(o in Organization, where: o.id == ^org_id)
    |> Repo.update_all(inc: [current_leads_count: 1])
  end

  def decrement_leads_count(org_id) do
    from(o in Organization, where: o.id == ^org_id)
    |> Repo.update_all(inc: [current_leads_count: -1])
  end

  def increment_users_count(org_id) do
    from(o in Organization, where: o.id == ^org_id)
    |> Repo.update_all(inc: [current_users_count: 1])
  end

  def check_leads_limit(org_id) do
    org = get_organization!(org_id)
    
    cond do
      org.plan == "enterprise" -> :ok
      org.current_leads_count < org.max_leads -> :ok
      true -> {:error, :leads_limit_reached}
    end
  end

  def check_users_limit(org_id) do
    org = get_organization!(org_id)
    
    cond do
      org.plan == "enterprise" -> :ok
      org.current_users_count < org.max_users -> :ok
      true -> {:error, :users_limit_reached}
    end
  end

  # ============================================
  # Feature Flags per Tenant
  # ============================================

  def has_feature?(org, feature) when is_binary(feature) do
    feature in (org.features || [])
  end

  def has_feature?(org_id, feature) when is_binary(org_id) do
    org = get_organization!(org_id)
    has_feature?(org, feature)
  end
end
```

---

## 3. Tenant Resolution Plug

```elixir
# lib/edu_consult_crm_web/plugs/tenant_plug.ex
defmodule EduConsultCrmWeb.Plugs.TenantPlug do
  @moduledoc """
  Resolves tenant (organization) from X-API-Key header.
  Must be placed BEFORE auth pipeline in router.
  """
  
  import Plug.Conn
  alias EduConsultCrm.Tenants

  @api_key_header "x-api-key"
  @cache_ttl :timer.minutes(5)

  def init(opts), do: opts

  def call(conn, _opts) do
    with {:ok, api_key} <- extract_api_key(conn),
         {:ok, org} <- resolve_organization(api_key) do
      conn
      |> assign(:current_org, org)
      |> assign(:org_id, org.id)
      |> put_tenant_in_process(org)
    else
      {:error, :missing_api_key} ->
        conn
        |> send_error(401, "Missing X-API-Key header")
        |> halt()

      {:error, :invalid_api_key} ->
        conn
        |> send_error(401, "Invalid API key")
        |> halt()

      {:error, {:suspended, reason}} ->
        conn
        |> send_error(403, "Organization suspended: #{reason}")
        |> halt()

      {:error, :subscription_expired} ->
        conn
        |> send_error(403, "Subscription expired. Please renew.")
        |> halt()
    end
  end

  defp extract_api_key(conn) do
    case get_req_header(conn, @api_key_header) do
      [api_key | _] when byte_size(api_key) > 0 -> {:ok, api_key}
      _ -> {:error, :missing_api_key}
    end
  end

  defp resolve_organization(api_key) do
    # Try cache first
    case get_cached_org(api_key) do
      {:ok, org} -> 
        {:ok, org}
      :miss ->
        # Fetch from DB and cache
        case Tenants.get_organization_by_api_key(api_key) do
          {:ok, org} ->
            cache_org(api_key, org)
            {:ok, org}
          error -> error
        end
    end
  end

  # Cache helpers (using Cachex)
  defp get_cached_org(api_key) do
    case Cachex.get(:org_cache, api_key) do
      {:ok, nil} -> :miss
      {:ok, org} -> {:ok, org}
      _ -> :miss
    end
  end

  defp cache_org(api_key, org) do
    Cachex.put(:org_cache, api_key, org, ttl: @cache_ttl)
  end

  # Store in process dictionary for Repo access
  defp put_tenant_in_process(conn, org) do
    Process.put(:current_org_id, org.id)
    Process.put(:current_org, org)
    conn
  end

  defp send_error(conn, status, message) do
    conn
    |> put_resp_content_type("application/json")
    |> send_resp(status, Jason.encode!(%{
      status: false,
      error: message,
      code: error_code(status)
    }))
  end

  defp error_code(401), do: "UNAUTHORIZED"
  defp error_code(403), do: "FORBIDDEN"
  defp error_code(_), do: "ERROR"
end
```

### Verify User Belongs to Tenant

```elixir
# lib/edu_consult_crm_web/plugs/tenant_scope_plug.ex
defmodule EduConsultCrmWeb.Plugs.TenantScopePlug do
  @moduledoc """
  Verifies that authenticated user belongs to the resolved tenant.
  Must be placed AFTER both TenantPlug and AuthPipeline.
  """
  
  import Plug.Conn
  alias EduConsultCrm.Accounts.Guardian

  def init(opts), do: opts

  def call(conn, _opts) do
    org = conn.assigns[:current_org]
    user = Guardian.Plug.current_resource(conn)

    cond do
      is_nil(org) ->
        send_error(conn, 401, "Tenant not resolved")

      is_nil(user) ->
        send_error(conn, 401, "User not authenticated")

      user.organization_id != org.id ->
        # User's JWT is for different org than API key
        send_error(conn, 403, "User does not belong to this organization")

      not user.is_active ->
        send_error(conn, 403, "User account is disabled")

      true ->
        conn
    end
  end

  defp send_error(conn, status, message) do
    conn
    |> put_resp_content_type("application/json")
    |> send_resp(status, Jason.encode!(%{status: false, error: message}))
    |> halt()
  end
end
```

---

## 4. Router Configuration

```elixir
# lib/edu_consult_crm_web/router.ex
defmodule EduConsultCrmWeb.Router do
  use EduConsultCrmWeb, :router

  # ============================================
  # Pipelines
  # ============================================

  pipeline :api do
    plug :accepts, ["json"]
    plug CORSPlug
  end

  # Resolves tenant from API key (no auth required)
  pipeline :tenant do
    plug EduConsultCrmWeb.Plugs.TenantPlug
  end

  # Full auth + tenant verification
  pipeline :authenticated do
    plug EduConsultCrm.Accounts.AuthPipeline
    plug EduConsultCrmWeb.Plugs.TenantScopePlug
  end

  # ============================================
  # Public Routes (No Auth, No Tenant)
  # ============================================
  
  scope "/api/v1", EduConsultCrmWeb.Api.V1, as: :api_v1 do
    pipe_through :api

    # Health check
    get "/health", HealthController, :check
    
    # App version check (no tenant needed)
    get "/app/version", AppController, :version
  end

  # ============================================
  # Tenant Routes (API Key Required, No User Auth)
  # ============================================
  
  scope "/api/v1", EduConsultCrmWeb.Api.V1, as: :api_v1 do
    pipe_through [:api, :tenant]

    # Auth endpoints - need tenant but not user auth
    post "/oauth/token", AuthController, :login
    post "/auth/register/sendOTP", AuthController, :send_otp
    post "/auth/register/verifyOTP", AuthController, :verify_otp
    post "/auth/register", AuthController, :register
    post "/auth/refresh", AuthController, :refresh
  end

  # ============================================
  # Protected Routes (API Key + User Auth)
  # ============================================
  
  scope "/api/v1", EduConsultCrmWeb.Api.V1, as: :api_v1 do
    pipe_through [:api, :tenant, :authenticated]

    # User/Employee
    post "/auth/logout", AuthController, :logout
    post "/employee/updateFCM", EmployeeController, :update_fcm
    post "/employee/settings/save", EmployeeController, :save_settings
    post "/employee/get/customer/employees", EmployeeController, :list
    get "/employee/me", EmployeeController, :me

    # Leads
    post "/lead/getData", LeadController, :index
    post "/lead/save", LeadController, :create_or_update
    post "/lead/saveNote", LeadController, :save_note
    post "/lead/status", LeadController, :list_statuses
    post "/lead/getByNumber", LeadController, :get_by_number
    post "/lead/allTags", LeadController, :all_tags
    # ... rest of lead routes

    # Calls
    post "/callLog/sync", CallController, :sync
    post "/callLog/sync/note", CallController, :sync_notes
    post "/callRecording/sync", CallController, :sync_recordings
    # ... rest of call routes

    # Templates
    post "/callnote/template/fetchAll", TemplateController, :list_note_templates
    post "/messagetemplate/fetchAll", TemplateController, :list_message_templates
    # ... rest of template routes

    # Courses (read from shared pool, but track per tenant)
    get "/courses", CourseController, :index
    get "/institutions", InstitutionController, :index
    
    # Settings
    get "/app/getSettings", SettingsController, :app_settings
    get "/feature/getStatus", SettingsController, :feature_status
  end

  # ============================================
  # Admin Routes (API Key + Admin Role)
  # ============================================
  
  scope "/api/v1/admin", EduConsultCrmWeb.Api.V1.Admin, as: :admin do
    pipe_through [:api, :tenant, :authenticated, :require_admin]

    # Organization settings
    get "/organization", OrganizationController, :show
    put "/organization", OrganizationController, :update
    post "/organization/rotate-api-key", OrganizationController, :rotate_api_key

    # User management
    resources "/users", UserController, except: [:new, :edit]
    post "/users/:id/activate", UserController, :activate
    post "/users/:id/deactivate", UserController, :deactivate

    # Lead statuses management
    resources "/lead-statuses", LeadStatusController, except: [:new, :edit]

    # Reports
    get "/reports/dashboard", ReportController, :dashboard
    get "/reports/leads", ReportController, :leads_report
    get "/reports/calls", ReportController, :calls_report
    get "/reports/performance", ReportController, :performance_report
  end
end
```

---

## 5. Tenant-Scoped Repo

```elixir
# lib/edu_consult_crm/repo.ex
defmodule EduConsultCrm.Repo do
  use Ecto.Repo,
    otp_app: :edu_consult_crm,
    adapter: Ecto.Adapters.Postgres

  import Ecto.Query

  @tenant_schemas [
    EduConsultCrm.Crm.Lead,
    EduConsultCrm.Crm.LeadNote,
    EduConsultCrm.Crm.LeadStatus,
    EduConsultCrm.Crm.LeadActivity,
    EduConsultCrm.Calls.CallLog,
    EduConsultCrm.Accounts.User,
    EduConsultCrm.Tenants.Branch,
    EduConsultCrm.Templates.NoteTemplate,
    EduConsultCrm.Templates.MessageTemplate
  ]

  # ============================================
  # Tenant Context Helpers
  # ============================================

  def current_org_id do
    Process.get(:current_org_id)
  end

  def current_org do
    Process.get(:current_org)
  end

  def put_org_id(org_id) do
    Process.put(:current_org_id, org_id)
  end

  # ============================================
  # Overridden Callbacks with Tenant Scoping
  # ============================================

  @doc """
  Wraps queries to automatically scope by organization_id
  """
  def scoped_query(queryable) do
    if tenant_scoped?(queryable) && current_org_id() do
      from q in queryable, where: q.organization_id == ^current_org_id()
    else
      queryable
    end
  end

  defp tenant_scoped?(queryable) do
    schema = get_schema(queryable)
    schema in @tenant_schemas
  end

  defp get_schema(%Ecto.Query{from: %{source: {_, schema}}}), do: schema
  defp get_schema(schema) when is_atom(schema), do: schema
  defp get_schema(_), do: nil

  # ============================================
  # Scoped Query Functions
  # ============================================

  @doc """
  Get all records scoped to current tenant
  """
  def all_scoped(queryable, opts \\ []) do
    queryable
    |> scoped_query()
    |> all(opts)
  end

  @doc """
  Get one record scoped to current tenant
  """
  def one_scoped(queryable, opts \\ []) do
    queryable
    |> scoped_query()
    |> one(opts)
  end

  @doc """
  Get record by ID, ensuring it belongs to current tenant
  """
  def get_scoped(queryable, id, opts \\ []) do
    queryable
    |> scoped_query()
    |> where([q], q.id == ^id)
    |> one(opts)
  end

  def get_scoped!(queryable, id, opts \\ []) do
    case get_scoped(queryable, id, opts) do
      nil -> raise Ecto.NoResultsError, queryable: queryable
      record -> record
    end
  end

  @doc """
  Insert with automatic organization_id
  """
  def insert_scoped(changeset, opts \\ []) do
    org_id = current_org_id()
    
    if org_id && tenant_scoped?(changeset.data.__struct__) do
      changeset
      |> Ecto.Changeset.put_change(:organization_id, org_id)
      |> insert(opts)
    else
      insert(changeset, opts)
    end
  end

  @doc """
  Update ensuring record belongs to current tenant
  """
  def update_scoped(changeset, opts \\ []) do
    record = changeset.data
    
    if tenant_scoped?(record.__struct__) && record.organization_id != current_org_id() do
      {:error, :unauthorized}
    else
      update(changeset, opts)
    end
  end

  @doc """
  Delete ensuring record belongs to current tenant
  """
  def delete_scoped(record, opts \\ []) do
    if tenant_scoped?(record.__struct__) && record.organization_id != current_org_id() do
      {:error, :unauthorized}
    else
      delete(record, opts)
    end
  end
end
```

---

## 6. Updated Contexts with Tenant Scoping

### CRM Context

```elixir
# lib/edu_consult_crm/crm/crm.ex
defmodule EduConsultCrm.Crm do
  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Crm.{Lead, LeadStatus, LeadNote, LeadActivity}
  alias EduConsultCrm.Tenants

  # ============================================
  # Leads - All queries auto-scoped
  # ============================================

  def list_leads(params \\ %{}) do
    Lead
    |> Repo.scoped_query()
    |> where([l], is_nil(l.deleted_at))
    |> apply_filters(params)
    |> apply_sorting(params)
    |> preload([:status, :assigned_user, :branch])
    |> paginate(params)
  end

  def get_lead!(id) do
    # Repo.get_scoped! ensures tenant isolation
    Lead
    |> Repo.get_scoped!(id)
    |> Repo.preload([:status, :assigned_user, :branch, :notes, :activities])
  end

  def get_lead_by_phone(phone) do
    Lead
    |> Repo.scoped_query()
    |> where([l], l.phone == ^phone or l.secondary_phone == ^phone)
    |> where([l], is_nil(l.deleted_at))
    |> Repo.one()
  end

  def create_lead(attrs, user) do
    # Check tenant limits
    with :ok <- Tenants.check_leads_limit(Repo.current_org_id()) do
      %Lead{}
      |> Lead.changeset(attrs)
      |> Ecto.Changeset.put_change(:created_by, user.id)
      |> Repo.insert_scoped()  # Auto-adds organization_id
      |> case do
        {:ok, lead} ->
          Tenants.increment_leads_count(Repo.current_org_id())
          log_activity(lead.id, user.id, "created", "Lead created")
          broadcast_lead_change(lead, :created)
          {:ok, lead}
        error -> error
      end
    end
  end

  def update_lead(%Lead{} = lead, attrs, user) do
    lead
    |> Lead.changeset(attrs)
    |> Repo.update_scoped()  # Verifies tenant ownership
    |> case do
      {:ok, updated_lead} ->
        broadcast_lead_change(updated_lead, :updated)
        {:ok, updated_lead}
      error -> error
    end
  end

  def delete_lead(id, user) do
    lead = get_lead!(id)
    
    # Soft delete
    lead
    |> Ecto.Changeset.change(%{deleted_at: DateTime.utc_now()})
    |> Repo.update_scoped()
    |> case do
      {:ok, _} ->
        Tenants.decrement_leads_count(Repo.current_org_id())
        log_activity(id, user.id, "deleted", "Lead deleted")
        :ok
      error -> error
    end
  end

  # ============================================
  # Lead Statuses - Tenant Specific
  # ============================================

  def list_statuses do
    LeadStatus
    |> Repo.scoped_query()
    |> where([s], s.is_active == true)
    |> order_by([s], s.order)
    |> Repo.all()
  end

  def create_status(attrs) do
    %LeadStatus{}
    |> LeadStatus.changeset(attrs)
    |> Repo.insert_scoped()
  end

  def get_default_status do
    LeadStatus
    |> Repo.scoped_query()
    |> where([s], s.is_default == true)
    |> Repo.one()
  end

  # ============================================
  # Stats - Scoped to Tenant
  # ============================================

  def get_lead_stats do
    org_id = Repo.current_org_id()
    today = Date.utc_today()
    week_ago = Date.add(today, -7)

    from(l in Lead,
      where: l.organization_id == ^org_id and is_nil(l.deleted_at),
      select: %{
        total: count(l.id),
        new_today: count(fragment("CASE WHEN DATE(?) = ? THEN 1 END", l.inserted_at, ^today)),
        new_this_week: count(fragment("CASE WHEN DATE(?) >= ? THEN 1 END", l.inserted_at, ^week_ago)),
        pending_follow_ups: count(fragment("CASE WHEN DATE(?) = ? THEN 1 END", l.next_follow_up_date, ^today)),
        overdue_follow_ups: count(fragment("CASE WHEN DATE(?) < ? THEN 1 END", l.next_follow_up_date, ^today))
      }
    )
    |> Repo.one()
  end

  # ============================================
  # Private Helpers
  # ============================================

  defp broadcast_lead_change(lead, event) do
    org_id = Repo.current_org_id()
    Phoenix.PubSub.broadcast(
      EduConsultCrm.PubSub,
      "org:#{org_id}:leads",
      {event, lead}
    )
  end

  defp log_activity(lead_id, user_id, type, description, metadata \\ %{}) do
    %LeadActivity{}
    |> LeadActivity.changeset(%{
      lead_id: lead_id,
      user_id: user_id,
      activity_type: type,
      description: description,
      metadata: metadata,
      created_at: DateTime.utc_now()
    })
    |> Repo.insert_scoped()
  end
end
```

---

## 7. Controller Usage

```elixir
# lib/edu_consult_crm_web/controllers/api/v1/lead_controller.ex
defmodule EduConsultCrmWeb.Api.V1.LeadController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Crm
  alias EduConsultCrm.Accounts.Guardian

  action_fallback EduConsultCrmWeb.FallbackController

  # No need to pass org_id - it's in process dictionary
  def index(conn, params) do
    # Current org already set by TenantPlug
    leads = Crm.list_leads(params)
    total = Crm.count_leads(params)

    render(conn, :index, leads: leads, total: total)
  end

  def create_or_update(conn, %{"leadDetails" => lead_params}) do
    user = Guardian.Plug.current_resource(conn)
    
    # Org ID automatically added by Repo.insert_scoped
    result = case lead_params["id"] do
      nil -> Crm.create_lead(lead_params, user)
      id -> 
        # get_lead! is scoped - will 404 if belongs to different org
        lead = Crm.get_lead!(id)
        Crm.update_lead(lead, lead_params, user)
    end

    case result do
      {:ok, lead} ->
        render(conn, :show, lead: lead)
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

  def show(conn, %{"id" => id}) do
    # Automatically scoped - returns 404 if not found in tenant
    lead = Crm.get_lead!(id)
    render(conn, :show, lead: lead)
  end

  # Feature flag check example
  def export(conn, params) do
    org = conn.assigns.current_org
    
    if Tenants.has_feature?(org, "export") do
      data = Crm.export_leads(params)
      send_download(conn, data)
    else
      conn
      |> put_status(:payment_required)
      |> render(:error, message: "Export feature not available in your plan")
    end
  end
end
```

---

## 8. Android Client Integration

### API Key Storage

```kotlin
// Store API key securely
class ApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "api_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("org_api_key", apiKey).apply()
    }

    fun getApiKey(): String? {
        return prefs.getString("org_api_key", null)
    }

    fun clearApiKey() {
        prefs.edit().remove("org_api_key").apply()
    }
}
```

### API Key Interceptor

```kotlin
// Add X-API-Key header to all requests
class ApiKeyInterceptor @Inject constructor(
    private val apiKeyManager: ApiKeyManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val apiKey = apiKeyManager.getApiKey()
            ?: throw ApiKeyMissingException("API key not configured")

        val authenticatedRequest = originalRequest.newBuilder()
            .header("X-API-Key", apiKey)
            .build()

        return chain.proceed(authenticatedRequest)
    }
}

class ApiKeyMissingException(message: String) : Exception(message)
```

### Network Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyInterceptor: ApiKeyInterceptor,
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)  // API key first
            .addInterceptor(authInterceptor)     // Then auth token
            .addInterceptor(loggingInterceptor)
            .build()
    }
}
```

### Onboarding Flow

```kotlin
// Organization setup screen
@Composable
fun OrganizationSetupScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter your Organization API Key",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Get this from your admin or organization settings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            placeholder = { Text("org_xxxxxxxxxxxxx") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.error != null
        )

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.validateAndSaveApiKey(apiKey) },
            modifier = Modifier.fillMaxWidth(),
            enabled = apiKey.startsWith("org_") && apiKey.length > 10
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Continue")
            }
        }
    }

    LaunchedEffect(uiState.isValidated) {
        if (uiState.isValidated) {
            onComplete()
        }
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val apiKeyManager: ApiKeyManager,
    private val settingsApi: SettingsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun validateAndSaveApiKey(apiKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Temporarily save to test
            apiKeyManager.saveApiKey(apiKey)

            try {
                // Try to fetch app settings - validates API key
                val response = settingsApi.getAppSettings()
                
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isValidated = true) }
                } else {
                    apiKeyManager.clearApiKey()
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Invalid API key. Please check and try again."
                        ) 
                    }
                }
            } catch (e: Exception) {
                apiKeyManager.clearApiKey()
                _uiState.update { 
                    it.copy(isLoading = false, error = "Connection failed. Check your internet.")
                }
            }
        }
    }
}
```

---

## 9. Plans & Limits Configuration

```elixir
# lib/edu_consult_crm/tenants/plans.ex
defmodule EduConsultCrm.Tenants.Plans do
  @plans %{
    "free" => %{
      name: "Free",
      max_users: 2,
      max_leads: 100,
      max_storage_gb: 0.5,
      features: ["basic_crm", "call_tracking"],
      price_monthly: 0
    },
    "starter" => %{
      name: "Starter",
      max_users: 5,
      max_leads: 500,
      max_storage_gb: 2,
      features: ["basic_crm", "call_tracking", "call_recording", "templates", "reports_basic"],
      price_monthly: 999  # INR
    },
    "pro" => %{
      name: "Professional",
      max_users: 15,
      max_leads: 5000,
      max_storage_gb: 10,
      features: [
        "basic_crm", "call_tracking", "call_recording", "templates",
        "reports_advanced", "whatsapp", "export", "api_access", "multi_branch"
      ],
      price_monthly: 2999
    },
    "enterprise" => %{
      name: "Enterprise",
      max_users: :unlimited,
      max_leads: :unlimited,
      max_storage_gb: 100,
      features: :all,
      price_monthly: :custom
    }
  }

  def get_plan(plan_id) do
    Map.get(@plans, plan_id, @plans["free"])
  end

  def get_limits(plan_id) do
    plan = get_plan(plan_id)
    %{
      max_users: plan.max_users,
      max_leads: plan.max_leads,
      max_storage_bytes: plan.max_storage_gb * 1_073_741_824
    }
  end

  def has_feature?(plan_id, feature) do
    plan = get_plan(plan_id)
    plan.features == :all || feature in plan.features
  end

  def all_plans do
    @plans
  end
end
```

---

## 10. Testing Multi-Tenancy

```elixir
# test/support/tenant_case.ex
defmodule EduConsultCrm.TenantCase do
  use ExUnit.CaseTemplate

  using do
    quote do
      alias EduConsultCrm.Repo

      setup do
        # Create test organization
        {:ok, org} = EduConsultCrm.Tenants.create_organization(%{
          name: "Test Org",
          slug: "test-org-#{System.unique_integer()}"
        })

        # Set tenant context
        Repo.put_org_id(org.id)

        {:ok, org: org}
      end
    end
  end
end

# test/edu_consult_crm/crm/crm_test.exs
defmodule EduConsultCrm.CrmTest do
  use EduConsultCrm.TenantCase

  alias EduConsultCrm.Crm

  describe "tenant isolation" do
    test "leads are scoped to organization", %{org: org1} do
      # Create lead in org1
      {:ok, user1} = create_user(org1)
      {:ok, lead1} = Crm.create_lead(%{first_name: "John", phone: "1234567890"}, user1)

      # Create second org
      {:ok, org2} = EduConsultCrm.Tenants.create_organization(%{
        name: "Other Org",
        slug: "other-org-#{System.unique_integer()}"
      })

      # Switch to org2
      Repo.put_org_id(org2.id)
      {:ok, user2} = create_user(org2)
      {:ok, lead2} = Crm.create_lead(%{first_name: "Jane", phone: "0987654321"}, user2)

      # Org2 should only see their lead
      assert length(Crm.list_leads()) == 1
      assert hd(Crm.list_leads()).id == lead2.id

      # Trying to access org1's lead should fail
      assert_raise Ecto.NoResultsError, fn ->
        Crm.get_lead!(lead1.id)
      end

      # Switch back to org1
      Repo.put_org_id(org1.id)
      
      # Org1 should only see their lead
      assert length(Crm.list_leads()) == 1
      assert hd(Crm.list_leads()).id == lead1.id
    end
  end
end
```

---

## Summary

| Component | Implementation |
|-----------|----------------|
| **Tenant Identification** | `X-API-Key` header |
| **Resolution** | `TenantPlug` with caching |
| **Data Isolation** | Row-level `organization_id` |
| **Query Scoping** | `Repo.scoped_query/1` |
| **Auto-tagging Inserts** | `Repo.insert_scoped/2` |
| **Cross-tenant Prevention** | `Repo.update_scoped/2`, `Repo.delete_scoped/2` |
| **User Verification** | `TenantScopePlug` checks user.org = request.org |
| **Feature Flags** | Per-tenant `features` array |
| **Usage Limits** | `max_users`, `max_leads`, storage |
| **Plans** | free, starter, pro, enterprise |

---

*Document Version: 1.0*  
*Multi-tenancy: API Key Based*
