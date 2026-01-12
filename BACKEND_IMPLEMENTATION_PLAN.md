# Educational Consultancy CRM - Backend Implementation Plan
## Elixir + Phoenix Framework

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Elixir 1.16+ |
| Framework | Phoenix 1.7+ |
| Database | PostgreSQL 15+ |
| Cache | Redis |
| Background Jobs | Oban |
| Real-time | Phoenix Channels |
| Admin UI | Phoenix LiveView |
| Auth | Guardian + Bcrypt |
| File Storage | AWS S3 / Cloudflare R2 |
| Search | PostgreSQL Full-Text / Meilisearch |
| API Docs | OpenAPI / Swagger |
| Deployment | Docker + Fly.io / AWS ECS |

---

# PHASE 1: FOUNDATION (Weeks 1-3)

## Week 1: Project Setup

### Day 1-2: Project Initialization

#### Tasks
```bash
# Create Phoenix project
mix phx.new edu_consult_crm --database postgresql --live

# Add dependencies to mix.exs
{:guardian, "~> 2.3"},
{:bcrypt_elixir, "~> 3.0"},
{:oban, "~> 2.17"},
{:ex_aws, "~> 2.4"},
{:ex_aws_s3, "~> 2.4"},
{:tesla, "~> 1.8"},
{:jason, "~> 1.4"},
{:cors_plug, "~> 3.0"},
{:ex_phone_number, "~> 0.4"},
{:timex, "~> 3.7"},
{:faker, "~> 0.18", only: [:dev, :test]},
{:ex_machina, "~> 2.7", only: :test},
{:credo, "~> 1.7", only: [:dev, :test]},
{:dialyxir, "~> 1.4", only: [:dev, :test]}
```

#### Project Structure
```
edu_consult_crm/
├── lib/
│   ├── edu_consult_crm/
│   │   ├── accounts/           # User & Auth context
│   │   │   ├── user.ex
│   │   │   ├── organization.ex
│   │   │   ├── branch.ex
│   │   │   └── token.ex
│   │   ├── crm/                # Core CRM context
│   │   │   ├── lead.ex
│   │   │   ├── lead_status.ex
│   │   │   ├── lead_tag.ex
│   │   │   ├── lead_note.ex
│   │   │   └── lead_activity.ex
│   │   ├── calls/              # Call tracking context
│   │   │   ├── call_log.ex
│   │   │   ├── call_recording.ex
│   │   │   └── call_note.ex
│   │   ├── education/          # Courses & Institutions
│   │   │   ├── course.ex
│   │   │   ├── institution.ex
│   │   │   └── country.ex
│   │   ├── templates/          # Message templates
│   │   │   ├── note_template.ex
│   │   │   └── message_template.ex
│   │   ├── notifications/      # Push notifications
│   │   │   ├── notification.ex
│   │   │   └── fcm_sender.ex
│   │   ├── workers/            # Oban workers
│   │   │   ├── sync_worker.ex
│   │   │   ├── reminder_worker.ex
│   │   │   └── recording_worker.ex
│   │   └── application.ex
│   │
│   ├── edu_consult_crm_web/
│   │   ├── controllers/
│   │   │   ├── api/v1/
│   │   │   │   ├── auth_controller.ex
│   │   │   │   ├── lead_controller.ex
│   │   │   │   ├── call_controller.ex
│   │   │   │   ├── course_controller.ex
│   │   │   │   └── employee_controller.ex
│   │   │   └── fallback_controller.ex
│   │   ├── live/               # LiveView admin
│   │   │   ├── dashboard_live.ex
│   │   │   ├── lead_live/
│   │   │   └── reports_live/
│   │   ├── channels/
│   │   │   ├── user_socket.ex
│   │   │   └── lead_channel.ex
│   │   ├── plugs/
│   │   │   ├── auth_plug.ex
│   │   │   └── organization_plug.ex
│   │   └── router.ex
│   │
│   └── edu_consult_crm.ex
│
├── priv/
│   └── repo/
│       └── migrations/
├── test/
├── config/
└── mix.exs
```

### Day 3-4: Database Schema Design

#### Migrations

```elixir
# priv/repo/migrations/001_create_organizations.exs
defmodule EduConsultCrm.Repo.Migrations.CreateOrganizations do
  use Ecto.Migration

  def change do
    create table(:organizations, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :name, :string, null: false
      add :slug, :string, null: false
      add :email, :string
      add :phone, :string
      add :address, :text
      add :logo_url, :string
      add :settings, :map, default: %{}
      add :subscription_plan, :string, default: "free"
      add :subscription_expires_at, :utc_datetime
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create unique_index(:organizations, [:slug])
  end
end

# priv/repo/migrations/002_create_branches.exs
defmodule EduConsultCrm.Repo.Migrations.CreateBranches do
  use Ecto.Migration

  def change do
    create table(:branches, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :name, :string, null: false
      add :code, :string
      add :address, :text
      add :phone, :string
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:branches, [:organization_id])
  end
end

# priv/repo/migrations/003_create_users.exs
defmodule EduConsultCrm.Repo.Migrations.CreateUsers do
  use Ecto.Migration

  def change do
    create table(:users, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :branch_id, references(:branches, type: :binary_id, on_delete: :nilify_all)
      add :email, :string, null: false
      add :phone, :string, null: false
      add :password_hash, :string, null: false
      add :first_name, :string, null: false
      add :last_name, :string
      add :role, :string, null: false, default: "agent"  # admin, manager, counselor, agent
      add :avatar_url, :string
      add :fcm_token, :string
      add :device_info, :map
      add :last_login_at, :utc_datetime
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create unique_index(:users, [:email, :organization_id])
    create unique_index(:users, [:phone, :organization_id])
    create index(:users, [:organization_id])
    create index(:users, [:branch_id])
  end
end

# priv/repo/migrations/004_create_lead_statuses.exs
defmodule EduConsultCrm.Repo.Migrations.CreateLeadStatuses do
  use Ecto.Migration

  def change do
    create table(:lead_statuses, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :name, :string, null: false
      add :code, :string, null: false
      add :color, :string, default: "#2196F3"
      add :order, :integer, default: 0
      add :is_default, :boolean, default: false
      add :is_closed, :boolean, default: false  # Won/Lost status
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create unique_index(:lead_statuses, [:organization_id, :code])
    create index(:lead_statuses, [:organization_id])
  end
end

# priv/repo/migrations/005_create_leads.exs
defmodule EduConsultCrm.Repo.Migrations.CreateLeads do
  use Ecto.Migration

  def change do
    create table(:leads, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :branch_id, references(:branches, type: :binary_id, on_delete: :nilify_all)
      add :assigned_to, references(:users, type: :binary_id, on_delete: :nilify_all)
      add :status_id, references(:lead_statuses, type: :binary_id, on_delete: :restrict), null: false
      add :created_by, references(:users, type: :binary_id, on_delete: :nilify_all)

      # Personal Info
      add :first_name, :string, null: false
      add :last_name, :string
      add :phone, :string, null: false
      add :secondary_phone, :string
      add :country_code, :integer, default: 91
      add :email, :string

      # Student Info
      add :student_name, :string
      add :parent_name, :string
      add :relationship, :string  # self, parent, guardian
      add :date_of_birth, :date

      # Educational Background
      add :current_education, :string  # 10th, 12th, Graduate, etc.
      add :current_institution, :string
      add :percentage, :decimal
      add :stream, :string  # Science, Commerce, Arts
      add :graduation_year, :integer

      # Inquiry Details
      add :interested_courses, {:array, :string}, default: []
      add :preferred_countries, {:array, :string}, default: []
      add :preferred_institutions, {:array, :string}, default: []
      add :budget_min, :integer
      add :budget_max, :integer
      add :budget_currency, :string, default: "INR"
      add :intake_preference, :string  # Fall 2025, Spring 2026

      # Lead Meta
      add :priority, :string, default: "medium"  # hot, warm, cold
      add :source, :string  # walk_in, call, website, referral, social_media
      add :source_details, :string  # Specific campaign, referrer name, etc.
      add :tags, {:array, :string}, default: []

      # Follow-up
      add :last_contact_date, :utc_datetime
      add :next_follow_up_date, :utc_datetime
      add :reminder_note, :text

      # Stats (denormalized for performance)
      add :total_calls, :integer, default: 0
      add :total_attempts, :integer, default: 0
      add :last_call_duration, :integer, default: 0

      # Custom Fields
      add :custom_fields, :map, default: %{}

      # Soft delete
      add :deleted_at, :utc_datetime

      timestamps(type: :utc_datetime)
    end

    create index(:leads, [:organization_id])
    create index(:leads, [:branch_id])
    create index(:leads, [:assigned_to])
    create index(:leads, [:status_id])
    create index(:leads, [:phone])
    create index(:leads, [:email])
    create index(:leads, [:next_follow_up_date])
    create index(:leads, [:created_at])
    create index(:leads, [:priority])
    create index(:leads, [:source])

    # Full-text search index
    execute """
    CREATE INDEX leads_search_idx ON leads 
    USING gin(to_tsvector('english', 
      coalesce(first_name, '') || ' ' || 
      coalesce(last_name, '') || ' ' || 
      coalesce(phone, '') || ' ' || 
      coalesce(email, '') || ' ' ||
      coalesce(student_name, '')
    ))
    """
  end
end

# priv/repo/migrations/006_create_lead_notes.exs
defmodule EduConsultCrm.Repo.Migrations.CreateLeadNotes do
  use Ecto.Migration

  def change do
    create table(:lead_notes, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :lead_id, references(:leads, type: :binary_id, on_delete: :delete_all), null: false
      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all)
      add :call_log_id, references(:call_logs, type: :binary_id, on_delete: :nilify_all)
      add :content, :text, null: false
      add :note_type, :string, default: "general"  # general, call, status_change, follow_up

      timestamps(type: :utc_datetime)
    end

    create index(:lead_notes, [:lead_id])
    create index(:lead_notes, [:user_id])
  end
end

# priv/repo/migrations/007_create_call_logs.exs
defmodule EduConsultCrm.Repo.Migrations.CreateCallLogs do
  use Ecto.Migration

  def change do
    create table(:call_logs, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all), null: false
      add :lead_id, references(:leads, type: :binary_id, on_delete: :nilify_all)
      add :phone_number, :string, null: false
      add :call_type, :string, null: false  # incoming, outgoing, missed
      add :duration, :integer, default: 0  # seconds
      add :call_at, :utc_datetime, null: false
      add :sim_slot, :integer
      add :device_call_id, :string  # ID from device call log
      add :notes, :text

      timestamps(type: :utc_datetime)
    end

    create index(:call_logs, [:organization_id])
    create index(:call_logs, [:user_id])
    create index(:call_logs, [:lead_id])
    create index(:call_logs, [:phone_number])
    create index(:call_logs, [:call_at])
    create unique_index(:call_logs, [:user_id, :device_call_id])
  end
end

# priv/repo/migrations/007b_create_call_recordings.exs
# Separate table for call recordings (Find → Compress → Upload pipeline)
defmodule EduConsultCrm.Repo.Migrations.CreateCallRecordings do
  use Ecto.Migration

  def change do
    create table(:call_recordings, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :call_log_id, references(:call_logs, type: :binary_id, on_delete: :cascade), null: false
      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all), null: false
      
      # Recording metadata
      add :original_file_name, :string
      add :original_file_size, :bigint           # Size in bytes
      add :compressed_file_size, :bigint         # Size after compression
      add :duration, :integer                    # Duration in seconds
      add :format, :string                       # mp3, m4a, etc.
      add :bitrate, :integer                     # Bitrate in kbps
      
      # Storage
      add :storage_key, :string                  # S3/R2 object key
      add :storage_url, :string                  # Public or presigned URL
      add :storage_provider, :string, default: "s3"  # s3, r2, local
      
      # Status tracking
      add :status, :string, default: "pending"   # pending, uploaded, verified, failed
      add :upload_started_at, :utc_datetime
      add :upload_completed_at, :utc_datetime
      add :retry_count, :integer, default: 0
      add :last_error, :text
      
      # Retention
      add :expires_at, :utc_datetime             # For auto-cleanup
      add :is_deleted, :boolean, default: false

      timestamps(type: :utc_datetime)
    end

    create index(:call_recordings, [:organization_id])
    create index(:call_recordings, [:call_log_id])
    create index(:call_recordings, [:user_id])
    create index(:call_recordings, [:status])
    create index(:call_recordings, [:expires_at])
    create unique_index(:call_recordings, [:call_log_id], where: "is_deleted = false")
  end
end

# priv/repo/migrations/007c_create_recording_paths.exs
# Device-specific recording paths (server-side suggestions)
defmodule EduConsultCrm.Repo.Migrations.CreateRecordingPaths do
  use Ecto.Migration

  def change do
    create table(:recording_paths, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :manufacturer, :string, null: false    # samsung, xiaomi, oneplus
      add :model, :string                         # Optional: specific model
      add :path, :string, null: false            # File system path
      add :priority, :integer, default: 0        # Higher = check first
      add :success_count, :integer, default: 0   # How often recordings were found here
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:recording_paths, [:manufacturer])
    create unique_index(:recording_paths, [:manufacturer, :model, :path])
    
    # Seed default paths
    execute """
    INSERT INTO recording_paths (id, manufacturer, path, priority, inserted_at, updated_at) VALUES
      (gen_random_uuid(), 'samsung', '/storage/emulated/0/Recordings/Call', 10, NOW(), NOW()),
      (gen_random_uuid(), 'samsung', '/storage/emulated/0/Call', 5, NOW(), NOW()),
      (gen_random_uuid(), 'xiaomi', '/storage/emulated/0/MIUI/sound_recorder/call_rec', 10, NOW(), NOW()),
      (gen_random_uuid(), 'oneplus', '/storage/emulated/0/Record/PhoneRecord', 10, NOW(), NOW()),
      (gen_random_uuid(), 'oppo', '/storage/emulated/0/Recordings', 10, NOW(), NOW()),
      (gen_random_uuid(), 'vivo', '/storage/emulated/0/Record/Call', 10, NOW(), NOW()),
      (gen_random_uuid(), 'realme', '/storage/emulated/0/Recordings', 10, NOW(), NOW()),
      (gen_random_uuid(), 'default', '/storage/emulated/0/Recordings/Call', 5, NOW(), NOW()),
      (gen_random_uuid(), 'default', '/storage/emulated/0/Recordings', 3, NOW(), NOW())
    """
  end
end

# priv/repo/migrations/008_create_courses.exs
defmodule EduConsultCrm.Repo.Migrations.CreateCourses do
  use Ecto.Migration

  def change do
    create table(:institutions, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :name, :string, null: false
      add :country, :string, null: false
      add :city, :string
      add :type, :string  # university, college, school
      add :ranking, :integer
      add :website, :string
      add :logo_url, :string
      add :description, :text
      add :is_partner, :boolean, default: false
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:institutions, [:country])
    create index(:institutions, [:is_partner])

    create table(:courses, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :institution_id, references(:institutions, type: :binary_id, on_delete: :delete_all), null: false
      add :name, :string, null: false
      add :level, :string, null: false  # bachelors, masters, phd, diploma
      add :duration, :string  # "2 Years", "4 Years"
      add :duration_months, :integer
      add :tuition_fee, :integer
      add :currency, :string, default: "USD"
      add :intakes, {:array, :string}, default: []  # ["Fall", "Spring"]
      add :requirements, :map  # eligibility criteria
      add :description, :text
      add :application_deadline, :date
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:courses, [:institution_id])
    create index(:courses, [:level])
    create index(:courses, [:tuition_fee])
  end
end

# priv/repo/migrations/009_create_templates.exs
defmodule EduConsultCrm.Repo.Migrations.CreateTemplates do
  use Ecto.Migration

  def change do
    create table(:note_templates, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :name, :string, null: false
      add :content, :text, null: false
      add :category, :string  # call, follow_up, general
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create table(:message_templates, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all), null: false
      add :name, :string, null: false
      add :content, :text, null: false
      add :category, :string  # greeting, follow_up, offer, visa_info
      add :dynamic_fields, {:array, :string}, default: []
      add :whatsapp_enabled, :boolean, default: true
      add :sms_enabled, :boolean, default: true
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:note_templates, [:organization_id])
    create index(:message_templates, [:organization_id])
  end
end

# priv/repo/migrations/010_create_lead_activities.exs
defmodule EduConsultCrm.Repo.Migrations.CreateLeadActivities do
  use Ecto.Migration

  def change do
    create table(:lead_activities, primary_key: false) do
      add :id, :binary_id, primary_key: true
      add :lead_id, references(:leads, type: :binary_id, on_delete: :delete_all), null: false
      add :user_id, references(:users, type: :binary_id, on_delete: :nilify_all)
      add :activity_type, :string, null: false
      # Types: created, status_changed, assigned, note_added, call_made, 
      #        follow_up_set, email_sent, whatsapp_sent, document_uploaded
      add :description, :text
      add :metadata, :map, default: %{}
      add :created_at, :utc_datetime, null: false
    end

    create index(:lead_activities, [:lead_id])
    create index(:lead_activities, [:user_id])
    create index(:lead_activities, [:activity_type])
    create index(:lead_activities, [:created_at])
  end
end
```

### Day 5: Core Contexts

#### Accounts Context
```elixir
# lib/edu_consult_crm/accounts/accounts.ex
defmodule EduConsultCrm.Accounts do
  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Accounts.{User, Organization, Branch}

  # Users
  def get_user!(id), do: Repo.get!(User, id)
  
  def get_user_by_email(email, org_id) do
    Repo.get_by(User, email: email, organization_id: org_id)
  end

  def get_user_by_phone(phone, org_id) do
    Repo.get_by(User, phone: phone, organization_id: org_id)
  end

  def authenticate_user(email_or_phone, password, org_slug) do
    with {:ok, org} <- get_organization_by_slug(org_slug),
         {:ok, user} <- find_user(email_or_phone, org.id),
         true <- Bcrypt.verify_pass(password, user.password_hash) do
      {:ok, user}
    else
      _ -> {:error, :invalid_credentials}
    end
  end

  def create_user(attrs) do
    %User{}
    |> User.changeset(attrs)
    |> Repo.insert()
  end

  def update_user(%User{} = user, attrs) do
    user
    |> User.changeset(attrs)
    |> Repo.update()
  end

  def update_fcm_token(user_id, fcm_token) do
    User
    |> Repo.get!(user_id)
    |> User.fcm_changeset(%{fcm_token: fcm_token})
    |> Repo.update()
  end

  # Organizations
  def get_organization!(id), do: Repo.get!(Organization, id)
  
  def get_organization_by_slug(slug) do
    case Repo.get_by(Organization, slug: slug) do
      nil -> {:error, :not_found}
      org -> {:ok, org}
    end
  end

  def create_organization(attrs) do
    %Organization{}
    |> Organization.changeset(attrs)
    |> Repo.insert()
  end

  # Branches
  def list_branches(org_id) do
    Branch
    |> where([b], b.organization_id == ^org_id and b.is_active == true)
    |> Repo.all()
  end

  def get_branch!(id), do: Repo.get!(Branch, id)
end
```

---

## Week 2: Authentication & API Foundation

### Day 1-2: Guardian Setup

```elixir
# lib/edu_consult_crm/accounts/guardian.ex
defmodule EduConsultCrm.Accounts.Guardian do
  use Guardian, otp_app: :edu_consult_crm

  alias EduConsultCrm.Accounts

  def subject_for_token(%{id: id}, _claims) do
    {:ok, to_string(id)}
  end

  def resource_from_claims(%{"sub" => id}) do
    case Accounts.get_user!(id) do
      nil -> {:error, :resource_not_found}
      user -> {:ok, user}
    end
  rescue
    Ecto.NoResultsError -> {:error, :resource_not_found}
  end
end

# lib/edu_consult_crm/accounts/auth_pipeline.ex
defmodule EduConsultCrm.Accounts.AuthPipeline do
  use Guardian.Plug.Pipeline,
    otp_app: :edu_consult_crm,
    module: EduConsultCrm.Accounts.Guardian,
    error_handler: EduConsultCrm.Accounts.AuthErrorHandler

  plug Guardian.Plug.VerifyHeader, scheme: "Bearer"
  plug Guardian.Plug.EnsureAuthenticated
  plug Guardian.Plug.LoadResource
end

# lib/edu_consult_crm/accounts/auth_error_handler.ex
defmodule EduConsultCrm.Accounts.AuthErrorHandler do
  import Plug.Conn

  @behaviour Guardian.Plug.ErrorHandler

  @impl Guardian.Plug.ErrorHandler
  def auth_error(conn, {type, _reason}, _opts) do
    body = Jason.encode!(%{error: to_string(type), message: "Authentication failed"})
    
    conn
    |> put_resp_content_type("application/json")
    |> send_resp(401, body)
  end
end
```

### Day 3-4: Auth Controller

```elixir
# lib/edu_consult_crm_web/controllers/api/v1/auth_controller.ex
defmodule EduConsultCrmWeb.Api.V1.AuthController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Accounts
  alias EduConsultCrm.Accounts.Guardian

  action_fallback EduConsultCrmWeb.FallbackController

  def login(conn, %{"username" => username, "password" => password, "org_slug" => org_slug}) do
    case Accounts.authenticate_user(username, password, org_slug) do
      {:ok, user} ->
        {:ok, access_token, _claims} = Guardian.encode_and_sign(user, %{}, ttl: {1, :day})
        {:ok, refresh_token, _claims} = Guardian.encode_and_sign(user, %{typ: "refresh"}, ttl: {30, :day})

        Accounts.update_user(user, %{last_login_at: DateTime.utc_now()})

        conn
        |> put_status(:ok)
        |> render(:token, access_token: access_token, refresh_token: refresh_token, user: user)

      {:error, :invalid_credentials} ->
        conn
        |> put_status(:unauthorized)
        |> render(:error, message: "Invalid credentials")
    end
  end

  def refresh(conn, %{"refresh_token" => refresh_token}) do
    case Guardian.decode_and_verify(refresh_token, %{typ: "refresh"}) do
      {:ok, claims} ->
        case Guardian.resource_from_claims(claims) do
          {:ok, user} ->
            {:ok, new_access_token, _claims} = Guardian.encode_and_sign(user, %{}, ttl: {1, :day})

            conn
            |> put_status(:ok)
            |> render(:refresh, access_token: new_access_token)

          {:error, _} ->
            conn
            |> put_status(:unauthorized)
            |> render(:error, message: "Invalid refresh token")
        end

      {:error, _} ->
        conn
        |> put_status(:unauthorized)
        |> render(:error, message: "Invalid refresh token")
    end
  end

  def register(conn, %{"organization_id" => org_id} = params) do
    with {:ok, user} <- Accounts.create_user(Map.put(params, "organization_id", org_id)) do
      {:ok, access_token, _claims} = Guardian.encode_and_sign(user, %{}, ttl: {1, :day})
      {:ok, refresh_token, _claims} = Guardian.encode_and_sign(user, %{typ: "refresh"}, ttl: {30, :day})

      conn
      |> put_status(:created)
      |> render(:token, access_token: access_token, refresh_token: refresh_token, user: user)
    end
  end

  def send_otp(conn, %{"phone" => phone}) do
    otp = :rand.uniform(999999) |> Integer.to_string() |> String.pad_leading(6, "0")
    
    # Store OTP in cache (Redis)
    EduConsultCrm.Cache.put("otp:#{phone}", otp, ttl: 300)
    
    # Send OTP via SMS service
    EduConsultCrm.SMS.send(phone, "Your OTP is: #{otp}")

    conn
    |> put_status(:ok)
    |> render(:message, message: "OTP sent successfully")
  end

  def verify_otp(conn, %{"phone" => phone, "otp" => otp}) do
    case EduConsultCrm.Cache.get("otp:#{phone}") do
      ^otp ->
        EduConsultCrm.Cache.delete("otp:#{phone}")
        conn
        |> put_status(:ok)
        |> render(:verified, verified: true)

      _ ->
        conn
        |> put_status(:bad_request)
        |> render(:error, message: "Invalid OTP")
    end
  end

  def logout(conn, _params) do
    user = Guardian.Plug.current_resource(conn)
    Accounts.update_fcm_token(user.id, nil)
    
    conn
    |> Guardian.Plug.sign_out()
    |> put_status(:ok)
    |> render(:message, message: "Logged out successfully")
  end
end
```

### Day 5: Router & API Structure

```elixir
# lib/edu_consult_crm_web/router.ex
defmodule EduConsultCrmWeb.Router do
  use EduConsultCrmWeb, :router

  pipeline :api do
    plug :accepts, ["json"]
    plug CORSPlug
  end

  pipeline :authenticated do
    plug EduConsultCrm.Accounts.AuthPipeline
    plug EduConsultCrmWeb.Plugs.OrganizationPlug
  end

  # Public API routes
  scope "/api/v1", EduConsultCrmWeb.Api.V1, as: :api_v1 do
    pipe_through :api

    post "/oauth/token", AuthController, :login
    post "/auth/refresh", AuthController, :refresh
    post "/auth/register/sendOTP", AuthController, :send_otp
    post "/auth/register/verifyOTP", AuthController, :verify_otp
    post "/auth/register", AuthController, :register
  end

  # Protected API routes
  scope "/api/v1", EduConsultCrmWeb.Api.V1, as: :api_v1 do
    pipe_through [:api, :authenticated]

    post "/auth/logout", AuthController, :logout
    post "/employee/updateFCM", EmployeeController, :update_fcm

    # Leads
    post "/lead/getData", LeadController, :index
    post "/lead/save", LeadController, :create_or_update
    post "/lead/saveNote", LeadController, :save_note
    post "/lead/status", LeadController, :list_statuses
    post "/lead/getByNumber", LeadController, :get_by_number
    post "/lead/notContacted", LeadController, :not_contacted
    post "/lead/isDeleted", LeadController, :check_deleted
    post "/lead/restore", LeadController, :restore
    post "/lead/allTags", LeadController, :all_tags
    post "/lead/note", LeadController, :get_notes
    post "/lead/recent/notes", LeadController, :recent_notes
    post "/lead/callLogs/getActive", LeadController, :call_history
    post "/lead/callLogs/getSummary", LeadController, :call_summary
    post "/lead/callBack/totalDue", LeadController, :due_callbacks
    post "/lead/form/getDynamicComponentDetails", LeadController, :dynamic_form
    post "/lead/isAssignedToMe", LeadController, :check_assignment

    # Calls
    post "/callLog/sync", CallController, :sync
    post "/callLog/getDetails", CallController, :details
    post "/callLog/sync/note", CallController, :sync_notes
    post "/callLog/getByNotesUpdatedAtWeb", CallController, :notes_updated
    
    # Call Recordings (Find → Compress → Upload pipeline)
    post "/callRecording/sync", CallController, :sync_recordings
    post "/callRecording/suggestPaths", CallController, :suggest_paths
    post "/callRecording/availableSpace", CallController, :available_space
    post "/callRecording/getUploadUrl", CallController, :get_upload_url
    post "/callRecording/confirmUpload", CallController, :confirm_upload
    get "/callRecording/:id/stream", CallController, :stream_recording

    # Employees
    post "/employee/settings/save", EmployeeController, :save_settings
    post "/employee/get/customer/employees", EmployeeController, :list_employees
    post "/employee/get/callLogs", EmployeeController, :call_logs
    post "/employee/validateNumber", EmployeeController, :validate_number
    post "/employee/subscriptionDetail", EmployeeController, :subscription
    post "/employee/customer/excludeList", EmployeeController, :exclude_list

    # Templates
    post "/callnote/template/save", TemplateController, :save_note_templates
    post "/callnote/template/fetchAll", TemplateController, :list_note_templates
    post "/callnote/template/delete", TemplateController, :delete_note_templates
    post "/messagetemplate/fetchAll", TemplateController, :list_message_templates
    get "/messagetemplate/fetch", TemplateController, :get_message_template
    get "/messagetemplate/tag/fetchAll", TemplateController, :message_template_tags

    # Settings
    get "/app/getSettings", SettingsController, :app_settings
    get "/feature/getStatus", SettingsController, :feature_status
    post "/feature/subscription", SettingsController, :check_subscription

    # Courses
    get "/courses", CourseController, :index
    get "/courses/:id", CourseController, :show
    get "/institutions", InstitutionController, :index
    get "/institutions/:id", InstitutionController, :show
    get "/countries", CountryController, :index
  end

  # LiveView Admin Dashboard
  scope "/admin", EduConsultCrmWeb.Admin, as: :admin do
    pipe_through [:browser, :authenticated_admin]

    live "/", DashboardLive.Index, :index
    live "/leads", LeadLive.Index, :index
    live "/leads/:id", LeadLive.Show, :show
    live "/reports", ReportLive.Index, :index
    live "/settings", SettingsLive.Index, :index
  end
end
```

---

## Week 3: CRM Core - Leads

### Day 1-2: Lead Context

```elixir
# lib/edu_consult_crm/crm/crm.ex
defmodule EduConsultCrm.Crm do
  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Crm.{Lead, LeadStatus, LeadNote, LeadActivity}

  # Lead Queries
  def list_leads(org_id, params \\ %{}) do
    Lead
    |> where([l], l.organization_id == ^org_id and is_nil(l.deleted_at))
    |> apply_filters(params)
    |> apply_sorting(params)
    |> preload([:status, :assigned_user, :branch])
    |> paginate(params)
  end

  defp apply_filters(query, params) do
    query
    |> filter_by_status(params["status_id"])
    |> filter_by_assigned(params["assigned_to"])
    |> filter_by_priority(params["priority"])
    |> filter_by_source(params["source"])
    |> filter_by_branch(params["branch_id"])
    |> filter_by_date_range(params["created_from"], params["created_to"])
    |> filter_by_follow_up(params["follow_up_date"])
    |> filter_by_search(params["search"])
  end

  defp filter_by_status(query, nil), do: query
  defp filter_by_status(query, status_id) do
    where(query, [l], l.status_id == ^status_id)
  end

  defp filter_by_assigned(query, nil), do: query
  defp filter_by_assigned(query, "unassigned") do
    where(query, [l], is_nil(l.assigned_to))
  end
  defp filter_by_assigned(query, user_id) do
    where(query, [l], l.assigned_to == ^user_id)
  end

  defp filter_by_search(query, nil), do: query
  defp filter_by_search(query, search) do
    search_term = "%#{search}%"
    where(query, [l], 
      ilike(l.first_name, ^search_term) or
      ilike(l.last_name, ^search_term) or
      ilike(l.phone, ^search_term) or
      ilike(l.email, ^search_term) or
      ilike(l.student_name, ^search_term)
    )
  end

  defp paginate(query, %{"page" => page, "page_size" => size}) do
    page = String.to_integer(page)
    size = String.to_integer(size)
    
    query
    |> limit(^size)
    |> offset(^((page - 1) * size))
    |> Repo.all()
  end
  defp paginate(query, _), do: Repo.all(query)

  def get_lead!(id) do
    Lead
    |> Repo.get!(id)
    |> Repo.preload([:status, :assigned_user, :branch, :notes, :activities])
  end

  def get_lead_by_phone(phone, org_id) do
    Lead
    |> where([l], l.organization_id == ^org_id)
    |> where([l], l.phone == ^phone or l.secondary_phone == ^phone)
    |> where([l], is_nil(l.deleted_at))
    |> Repo.one()
  end

  def create_lead(attrs, user) do
    %Lead{}
    |> Lead.changeset(attrs)
    |> Ecto.Changeset.put_change(:organization_id, user.organization_id)
    |> Ecto.Changeset.put_change(:created_by, user.id)
    |> Repo.insert()
    |> case do
      {:ok, lead} ->
        log_activity(lead.id, user.id, "created", "Lead created")
        broadcast_lead_change(lead, :created)
        {:ok, lead}
      error -> error
    end
  end

  def update_lead(%Lead{} = lead, attrs, user) do
    old_status = lead.status_id

    lead
    |> Lead.changeset(attrs)
    |> Repo.update()
    |> case do
      {:ok, updated_lead} ->
        if updated_lead.status_id != old_status do
          log_activity(lead.id, user.id, "status_changed", 
            "Status changed from #{old_status} to #{updated_lead.status_id}",
            %{old_status: old_status, new_status: updated_lead.status_id}
          )
        end
        broadcast_lead_change(updated_lead, :updated)
        {:ok, updated_lead}
      error -> error
    end
  end

  def assign_lead(lead_id, assignee_id, user) do
    lead = get_lead!(lead_id)
    
    lead
    |> Ecto.Changeset.change(%{assigned_to: assignee_id})
    |> Repo.update()
    |> case do
      {:ok, updated_lead} ->
        log_activity(lead_id, user.id, "assigned", "Lead assigned to #{assignee_id}")
        notify_assignment(updated_lead, assignee_id)
        {:ok, updated_lead}
      error -> error
    end
  end

  def add_note(lead_id, content, user, call_log_id \\ nil) do
    %LeadNote{}
    |> LeadNote.changeset(%{
      lead_id: lead_id,
      user_id: user.id,
      content: content,
      call_log_id: call_log_id,
      note_type: if(call_log_id, do: "call", else: "general")
    })
    |> Repo.insert()
    |> case do
      {:ok, note} ->
        log_activity(lead_id, user.id, "note_added", "Note added")
        {:ok, note}
      error -> error
    end
  end

  def set_follow_up(lead_id, date, note, user) do
    lead = get_lead!(lead_id)
    
    lead
    |> Ecto.Changeset.change(%{
      next_follow_up_date: date,
      reminder_note: note
    })
    |> Repo.update()
    |> case do
      {:ok, updated_lead} ->
        log_activity(lead_id, user.id, "follow_up_set", "Follow-up scheduled for #{date}")
        schedule_reminder(updated_lead)
        {:ok, updated_lead}
      error -> error
    end
  end

  # Stats
  def get_lead_stats(org_id) do
    today = Date.utc_today()
    week_ago = Date.add(today, -7)

    query = from l in Lead,
      where: l.organization_id == ^org_id and is_nil(l.deleted_at),
      select: %{
        total: count(l.id),
        new_today: count(fragment("CASE WHEN DATE(?) = ? THEN 1 END", l.inserted_at, ^today)),
        new_this_week: count(fragment("CASE WHEN DATE(?) >= ? THEN 1 END", l.inserted_at, ^week_ago)),
        pending_follow_ups: count(fragment("CASE WHEN DATE(?) = ? THEN 1 END", l.next_follow_up_date, ^today)),
        overdue_follow_ups: count(fragment("CASE WHEN DATE(?) < ? THEN 1 END", l.next_follow_up_date, ^today))
      }

    Repo.one(query)
  end

  def get_leads_by_status(org_id) do
    from(l in Lead,
      where: l.organization_id == ^org_id and is_nil(l.deleted_at),
      join: s in LeadStatus, on: l.status_id == s.id,
      group_by: [s.id, s.name, s.color, s.order],
      select: %{
        status_id: s.id,
        status_name: s.name,
        color: s.color,
        count: count(l.id)
      },
      order_by: s.order
    )
    |> Repo.all()
  end

  # Activities
  def log_activity(lead_id, user_id, type, description, metadata \\ %{}) do
    %LeadActivity{}
    |> LeadActivity.changeset(%{
      lead_id: lead_id,
      user_id: user_id,
      activity_type: type,
      description: description,
      metadata: metadata,
      created_at: DateTime.utc_now()
    })
    |> Repo.insert()
  end

  def get_activities(lead_id, limit \\ 50) do
    LeadActivity
    |> where([a], a.lead_id == ^lead_id)
    |> order_by([a], desc: a.created_at)
    |> limit(^limit)
    |> preload(:user)
    |> Repo.all()
  end

  # Real-time broadcasts
  defp broadcast_lead_change(lead, event) do
    Phoenix.PubSub.broadcast(
      EduConsultCrm.PubSub,
      "org:#{lead.organization_id}:leads",
      {event, lead}
    )
  end
end
```

---

## Week 4: Calls Context (Find → Compress → Upload Pipeline)

> **Note:** The Android app finds recordings from the device's native recorder,
> compresses them locally using FFmpeg, then uploads to our cloud storage.
> The backend handles: suggested paths, presigned upload URLs, upload confirmation, and streaming.

### Day 1-2: Calls Context

```elixir
# lib/edu_consult_crm/calls/calls.ex
defmodule EduConsultCrm.Calls do
  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Calls.{CallLog, CallRecording, RecordingPath}
  alias EduConsultCrm.Storage

  # ============================================
  # Call Logs
  # ============================================

  def sync_call_logs(call_logs_params, user) do
    Enum.map(call_logs_params, fn params ->
      case find_or_create_call_log(params, user) do
        {:ok, call_log} -> {:ok, call_log}
        {:error, _} = error -> error
      end
    end)
  end

  defp find_or_create_call_log(params, user) do
    case Repo.get_by(CallLog, user_id: user.id, device_call_id: params["deviceCallId"]) do
      nil ->
        %CallLog{}
        |> CallLog.changeset(Map.merge(params, %{
          "organization_id" => user.organization_id,
          "user_id" => user.id
        }))
        |> Repo.insert()
      
      existing ->
        existing
        |> CallLog.changeset(params)
        |> Repo.update()
    end
  end

  def get_call_log!(id), do: Repo.get!(CallLog, id)

  def get_call_logs_for_lead(lead_id, opts \\ []) do
    limit = Keyword.get(opts, :limit, 50)
    
    CallLog
    |> where([c], c.lead_id == ^lead_id)
    |> order_by([c], desc: c.call_at)
    |> limit(^limit)
    |> preload(:recording)
    |> Repo.all()
  end

  # ============================================
  # Recording Path Suggestions (Device-specific)
  # ============================================

  def get_suggested_paths(%{"manufacturer" => manufacturer, "model" => model}) do
    # Try model-specific first, then manufacturer, then defaults
    paths = RecordingPath
    |> where([p], p.is_active == true)
    |> where([p], 
      (p.manufacturer == ^String.downcase(manufacturer) and (is_nil(p.model) or p.model == ^model)) or
      p.manufacturer == "default"
    )
    |> order_by([p], [desc: p.priority, desc: p.success_count])
    |> select([p], p.path)
    |> Repo.all()
    |> Enum.uniq()

    {:ok, paths}
  end

  def report_path_success(manufacturer, path) do
    RecordingPath
    |> where([p], p.manufacturer == ^String.downcase(manufacturer) and p.path == ^path)
    |> Repo.update_all(inc: [success_count: 1])
  end

  # ============================================
  # Recording Upload Flow
  # ============================================

  @doc """
  Generate a presigned URL for uploading a recording.
  Called by Android before uploading compressed file.
  """
  def get_upload_url(call_log_id, params, user) do
    call_log = get_call_log!(call_log_id)
    
    # Verify ownership
    unless call_log.user_id == user.id do
      {:error, :unauthorized}
    end

    # Generate unique storage key
    storage_key = generate_storage_key(user.organization_id, call_log_id, params["fileName"])
    
    # Create recording record in pending state
    {:ok, recording} = %CallRecording{}
    |> CallRecording.changeset(%{
      organization_id: user.organization_id,
      call_log_id: call_log_id,
      user_id: user.id,
      original_file_name: params["fileName"],
      original_file_size: params["fileSize"],
      format: get_format(params["fileName"]),
      storage_key: storage_key,
      status: "pending",
      upload_started_at: DateTime.utc_now()
    })
    |> Repo.insert(on_conflict: :replace_all, conflict_target: :call_log_id)

    # Generate presigned upload URL (S3/R2)
    {:ok, upload_url} = Storage.generate_presigned_upload_url(
      storage_key,
      params["contentType"] || "audio/mpeg",
      params["fileSize"]
    )

    {:ok, %{
      recording_id: recording.id,
      upload_url: upload_url,
      storage_key: storage_key,
      expires_in: 3600  # 1 hour
    }}
  end

  @doc """
  Confirm upload completion. Called by Android after S3 upload succeeds.
  """
  def confirm_upload(recording_id, params, user) do
    recording = Repo.get!(CallRecording, recording_id)
    
    unless recording.user_id == user.id do
      {:error, :unauthorized}
    end

    # Verify the file exists in storage
    case Storage.verify_object_exists(recording.storage_key) do
      {:ok, metadata} ->
        recording
        |> CallRecording.changeset(%{
          status: "uploaded",
          compressed_file_size: metadata.content_length,
          duration: params["duration"],
          bitrate: params["bitrate"],
          storage_url: Storage.get_public_url(recording.storage_key),
          upload_completed_at: DateTime.utc_now(),
          expires_at: calculate_expiry(user.organization_id)
        })
        |> Repo.update()
        
      {:error, :not_found} ->
        {:error, :upload_not_found}
    end
  end

  def get_recording_stream_url(recording_id, user) do
    recording = Repo.get!(CallRecording, recording_id)
    
    # Verify access (same org)
    unless recording.organization_id == user.organization_id do
      {:error, :unauthorized}
    end

    # Generate time-limited presigned URL for streaming
    {:ok, url} = Storage.generate_presigned_download_url(
      recording.storage_key,
      expires_in: 3600
    )

    {:ok, %{url: url, duration: recording.duration}}
  end

  # ============================================
  # Storage Cleanup (Oban Worker)
  # ============================================

  def cleanup_expired_recordings do
    now = DateTime.utc_now()
    
    expired = CallRecording
    |> where([r], r.expires_at < ^now and r.is_deleted == false)
    |> Repo.all()

    Enum.each(expired, fn recording ->
      # Delete from S3
      Storage.delete_object(recording.storage_key)
      
      # Mark as deleted
      recording
      |> Ecto.Changeset.change(%{is_deleted: true})
      |> Repo.update()
    end)

    {:ok, length(expired)}
  end

  # ============================================
  # Private Helpers
  # ============================================

  defp generate_storage_key(org_id, call_log_id, file_name) do
    ext = Path.extname(file_name)
    date = Date.utc_today()
    "recordings/#{org_id}/#{date.year}/#{date.month}/#{call_log_id}#{ext}"
  end

  defp get_format(file_name) do
    file_name
    |> Path.extname()
    |> String.trim_leading(".")
    |> String.downcase()
  end

  defp calculate_expiry(org_id) do
    # TODO: Get retention policy from org settings
    retention_days = 90
    DateTime.add(DateTime.utc_now(), retention_days * 24 * 3600, :second)
  end
end
```

### Day 3-4: Storage Module (S3/R2)

```elixir
# lib/edu_consult_crm/storage/storage.ex
defmodule EduConsultCrm.Storage do
  @moduledoc """
  Cloud storage abstraction for call recordings.
  Supports AWS S3 and Cloudflare R2.
  """

  @bucket Application.compile_env(:edu_consult_crm, :recording_bucket, "edu-consult-recordings")

  def generate_presigned_upload_url(key, content_type, content_length) do
    config = ExAws.Config.new(:s3)
    
    opts = [
      expires_in: 3600,
      content_type: content_type,
      content_length_range: {0, content_length * 2}  # Allow some buffer
    ]

    {:ok, url} = ExAws.S3.presigned_url(config, :put, @bucket, key, opts)
    {:ok, url}
  end

  def generate_presigned_download_url(key, opts \\ []) do
    config = ExAws.Config.new(:s3)
    expires_in = Keyword.get(opts, :expires_in, 3600)

    {:ok, url} = ExAws.S3.presigned_url(config, :get, @bucket, key, expires_in: expires_in)
    {:ok, url}
  end

  def verify_object_exists(key) do
    case ExAws.S3.head_object(@bucket, key) |> ExAws.request() do
      {:ok, %{headers: headers}} ->
        content_length = headers
        |> Enum.find(fn {k, _} -> String.downcase(k) == "content-length" end)
        |> elem(1)
        |> String.to_integer()

        {:ok, %{content_length: content_length}}
      
      {:error, _} ->
        {:error, :not_found}
    end
  end

  def delete_object(key) do
    ExAws.S3.delete_object(@bucket, key)
    |> ExAws.request()
  end

  def get_public_url(key) do
    # For public buckets or CDN
    cdn_url = Application.get_env(:edu_consult_crm, :cdn_url)
    "#{cdn_url}/#{key}"
  end

  def get_available_space(org_id) do
    # Calculate used storage for org
    used = EduConsultCrm.Repo.one(
      from r in EduConsultCrm.Calls.CallRecording,
      where: r.organization_id == ^org_id and r.is_deleted == false,
      select: coalesce(sum(r.compressed_file_size), 0)
    )

    # Get org's storage limit
    org = EduConsultCrm.Tenants.get_organization!(org_id)
    limit = org.storage_limit_bytes

    {:ok, %{
      used_bytes: used,
      limit_bytes: limit,
      available_bytes: max(limit - used, 0),
      used_percentage: Float.round(used / limit * 100, 2)
    }}
  end
end
```

### Day 5: Call Controller (Recording Endpoints)

```elixir
# lib/edu_consult_crm_web/controllers/api/v1/call_controller.ex
defmodule EduConsultCrmWeb.Api.V1.CallController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Calls
  alias EduConsultCrm.Accounts.Guardian

  action_fallback EduConsultCrmWeb.FallbackController

  # Sync call logs from device
  def sync(conn, %{"callLogs" => call_logs}) do
    user = Guardian.Plug.current_resource(conn)
    
    results = Calls.sync_call_logs(call_logs, user)
    synced_count = Enum.count(results, fn {status, _} -> status == :ok end)

    render(conn, :sync_result, synced: synced_count)
  end

  # Get suggested recording paths based on device info
  def suggest_paths(conn, params) do
    case Calls.get_suggested_paths(params) do
      {:ok, paths} ->
        # Report which manufacturer is asking (for analytics)
        if params["successPath"] do
          Calls.report_path_success(params["manufacturer"], params["successPath"])
        end
        
        render(conn, :paths, paths: paths)
      
      {:error, reason} ->
        conn
        |> put_status(:bad_request)
        |> render(:error, message: reason)
    end
  end

  # Get presigned URL for uploading recording
  def get_upload_url(conn, %{"callLogId" => call_log_id} = params) do
    user = Guardian.Plug.current_resource(conn)

    case Calls.get_upload_url(call_log_id, params, user) do
      {:ok, data} ->
        render(conn, :upload_url, data: data)

      {:error, :unauthorized} ->
        conn
        |> put_status(:forbidden)
        |> render(:error, message: "Not authorized to upload for this call")
    end
  end

  # Confirm upload completed
  def confirm_upload(conn, %{"recordingId" => recording_id} = params) do
    user = Guardian.Plug.current_resource(conn)

    case Calls.confirm_upload(recording_id, params, user) do
      {:ok, recording} ->
        render(conn, :recording, recording: recording)

      {:error, :upload_not_found} ->
        conn
        |> put_status(:bad_request)
        |> render(:error, message: "Upload not found in storage")

      {:error, :unauthorized} ->
        conn
        |> put_status(:forbidden)
        |> render(:error, message: "Not authorized")
    end
  end

  # Stream recording (returns presigned URL)
  def stream_recording(conn, %{"id" => recording_id}) do
    user = Guardian.Plug.current_resource(conn)

    case Calls.get_recording_stream_url(recording_id, user) do
      {:ok, data} ->
        # Option 1: Return URL for client to stream
        render(conn, :stream_url, data: data)
        
        # Option 2: Redirect to presigned URL
        # redirect(conn, external: data.url)

      {:error, :unauthorized} ->
        conn
        |> put_status(:forbidden)
        |> render(:error, message: "Not authorized to access this recording")
    end
  end

  # Check available storage space
  def available_space(conn, _params) do
    user = Guardian.Plug.current_resource(conn)

    case EduConsultCrm.Storage.get_available_space(user.organization_id) do
      {:ok, data} ->
        render(conn, :storage_info, data: data)
    end
  end
end
```

### Recording Cleanup Worker (Oban)

```elixir
# lib/edu_consult_crm/workers/recording_cleanup_worker.ex
defmodule EduConsultCrm.Workers.RecordingCleanupWorker do
  use Oban.Worker, queue: :maintenance, max_attempts: 3

  alias EduConsultCrm.Calls

  @impl Oban.Worker
  def perform(_job) do
    case Calls.cleanup_expired_recordings() do
      {:ok, count} ->
        if count > 0 do
          Logger.info("Cleaned up #{count} expired recordings")
        end
        :ok
      
      {:error, reason} ->
        {:error, reason}
    end
  end
end

# Schedule in config/config.exs
config :edu_consult_crm, Oban,
  plugins: [
    {Oban.Plugins.Cron, crontab: [
      {"0 3 * * *", EduConsultCrm.Workers.RecordingCleanupWorker}  # Daily at 3 AM
    ]}
  ]
```

---

### Day 3-4: Lead Controller

```elixir
# lib/edu_consult_crm_web/controllers/api/v1/lead_controller.ex
defmodule EduConsultCrmWeb.Api.V1.LeadController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Crm
  alias EduConsultCrm.Accounts.Guardian

  action_fallback EduConsultCrmWeb.FallbackController

  def index(conn, params) do
    user = Guardian.Plug.current_resource(conn)
    
    leads = Crm.list_leads(user.organization_id, params)
    total = Crm.count_leads(user.organization_id, params)

    render(conn, :index, leads: leads, total: total, params: params)
  end

  def create_or_update(conn, %{"leadDetails" => lead_params} = params) do
    user = Guardian.Plug.current_resource(conn)

    result = case lead_params["id"] do
      nil -> Crm.create_lead(lead_params, user)
      id -> 
        lead = Crm.get_lead!(id)
        Crm.update_lead(lead, lead_params, user)
    end

    case result do
      {:ok, lead} ->
        conn
        |> put_status(:ok)
        |> render(:show, lead: lead)
      {:error, changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> render(:error, changeset: changeset)
    end
  end

  def save_note(conn, %{"lead_id" => lead_id, "content" => content} = params) do
    user = Guardian.Plug.current_resource(conn)
    call_log_id = params["call_log_id"]

    case Crm.add_note(lead_id, content, user, call_log_id) do
      {:ok, note} ->
        # Update follow-up if provided
        if params["follow_up_date"] do
          Crm.set_follow_up(lead_id, params["follow_up_date"], params["reminder_note"], user)
        end

        # Update status if provided
        if params["status_id"] do
          lead = Crm.get_lead!(lead_id)
          Crm.update_lead(lead, %{status_id: params["status_id"]}, user)
        end

        render(conn, :note, note: note)

      {:error, changeset} ->
        conn
        |> put_status(:unprocessable_entity)
        |> render(:error, changeset: changeset)
    end
  end

  def list_statuses(conn, _params) do
    user = Guardian.Plug.current_resource(conn)
    statuses = Crm.list_statuses(user.organization_id)
    render(conn, :statuses, statuses: statuses)
  end

  def get_by_number(conn, %{"phone" => phone}) do
    user = Guardian.Plug.current_resource(conn)
    
    case Crm.get_lead_by_phone(phone, user.organization_id) do
      nil ->
        conn
        |> put_status(:not_found)
        |> render(:error, message: "Lead not found")
      lead ->
        render(conn, :show, lead: lead)
    end
  end

  def not_contacted(conn, params) do
    user = Guardian.Plug.current_resource(conn)
    leads = Crm.list_not_contacted(user.organization_id, params)
    render(conn, :index, leads: leads)
  end

  def call_history(conn, %{"leadId" => lead_id, "pageNo" => page, "pageSize" => size}) do
    calls = Crm.get_lead_call_history(lead_id, page, size)
    render(conn, :call_history, calls: calls)
  end

  def call_summary(conn, %{"leadId" => lead_id}) do
    summary = Crm.get_lead_call_summary(lead_id)
    render(conn, :call_summary, summary: summary)
  end

  def due_callbacks(conn, params) do
    user = Guardian.Plug.current_resource(conn)
    count = Crm.count_due_callbacks(user.organization_id, params)
    render(conn, :count, count: count)
  end

  def all_tags(conn, _params) do
    user = Guardian.Plug.current_resource(conn)
    tags = Crm.list_all_tags(user.organization_id)
    render(conn, :tags, tags: tags)
  end

  def dynamic_form(conn, _params) do
    user = Guardian.Plug.current_resource(conn)
    form = Crm.get_dynamic_form(user.organization_id)
    render(conn, :form, form: form)
  end
end
```

### Day 5: Lead JSON Views

```elixir
# lib/edu_consult_crm_web/controllers/api/v1/lead_json.ex
defmodule EduConsultCrmWeb.Api.V1.LeadJSON do
  alias EduConsultCrm.Crm.Lead

  def index(%{leads: leads, total: total, params: params}) do
    %{
      status: true,
      data: %{
        leads: Enum.map(leads, &lead_data/1),
        totalCount: total,
        pageNo: params["page"] || 1,
        pageSize: params["page_size"] || 20,
        hasMore: length(leads) == (params["page_size"] || 20)
      }
    }
  end

  def show(%{lead: lead}) do
    %{
      status: true,
      data: lead_data(lead)
    }
  end

  def statuses(%{statuses: statuses}) do
    %{
      status: true,
      data: Enum.map(statuses, fn s ->
        %{
          id: s.id,
          name: s.name,
          code: s.code,
          color: s.color,
          order: s.order,
          isClosed: s.is_closed
        }
      end)
    }
  end

  defp lead_data(lead) do
    %{
      id: lead.id,
      firstName: lead.first_name,
      lastName: lead.last_name,
      number: lead.phone,
      secondaryNumber: lead.secondary_phone,
      countryCode: lead.country_code,
      email: lead.email,
      studentName: lead.student_name,
      parentName: lead.parent_name,
      relationship: lead.relationship,
      dateOfBirth: lead.date_of_birth,
      currentEducation: lead.current_education,
      currentInstitution: lead.current_institution,
      percentage: lead.percentage,
      stream: lead.stream,
      graduationYear: lead.graduation_year,
      interestedCourses: lead.interested_courses,
      preferredCountries: lead.preferred_countries,
      preferredInstitutions: lead.preferred_institutions,
      budgetMin: lead.budget_min,
      budgetMax: lead.budget_max,
      budgetCurrency: lead.budget_currency,
      intakePreference: lead.intake_preference,
      status: lead.status && %{
        id: lead.status.id,
        name: lead.status.name,
        color: lead.status.color
      },
      priority: lead.priority,
      source: lead.source,
      tags: lead.tags,
      assignedTo: lead.assigned_user && %{
        id: lead.assigned_user.id,
        name: "#{lead.assigned_user.first_name} #{lead.assigned_user.last_name}"
      },
      lastContactDate: lead.last_contact_date,
      nextFollowUpDate: lead.next_follow_up_date,
      reminderNote: lead.reminder_note,
      totalCalls: lead.total_calls,
      totalAttempts: lead.total_attempts,
      customFields: lead.custom_fields,
      createdDate: lead.inserted_at,
      modifiedDate: lead.updated_at
    }
  end
end
```

---

# PHASE 2: CALL & SYNC (Weeks 4-5)

## Week 4: Call Tracking

### Day 1-2: Calls Context

```elixir
# lib/edu_consult_crm/calls/calls.ex
defmodule EduConsultCrm.Calls do
  import Ecto.Query
  alias EduConsultCrm.Repo
  alias EduConsultCrm.Calls.{CallLog, CallRecording}
  alias EduConsultCrm.Crm

  def sync_call_logs(user, call_logs) do
    results = Enum.map(call_logs, fn log ->
      sync_single_call_log(user, log)
    end)

    successful = Enum.count(results, fn {status, _} -> status == :ok end)
    {:ok, %{synced: successful, total: length(call_logs)}}
  end

  defp sync_single_call_log(user, log_data) do
    # Check if already exists
    existing = Repo.get_by(CallLog, 
      user_id: user.id, 
      device_call_id: log_data["deviceCallId"]
    )

    if existing do
      {:ok, existing}
    else
      # Find associated lead by phone number
      lead = Crm.get_lead_by_phone(log_data["phoneNumber"], user.organization_id)

      %CallLog{}
      |> CallLog.changeset(%{
        organization_id: user.organization_id,
        user_id: user.id,
        lead_id: lead && lead.id,
        phone_number: log_data["phoneNumber"],
        call_type: log_data["callType"],
        duration: log_data["duration"],
        call_at: parse_datetime(log_data["timestamp"]),
        sim_slot: log_data["simSlot"],
        device_call_id: log_data["deviceCallId"]
      })
      |> Repo.insert()
      |> case do
        {:ok, call_log} ->
          # Update lead stats
          if lead do
            update_lead_call_stats(lead, call_log)
          end
          {:ok, call_log}
        error -> error
      end
    end
  end

  defp update_lead_call_stats(lead, call_log) do
    Crm.update_lead_stats(lead.id, %{
      total_calls: lead.total_calls + 1,
      total_attempts: if(call_log.call_type == "missed", do: lead.total_attempts + 1, else: lead.total_attempts),
      last_call_duration: call_log.duration,
      last_contact_date: call_log.call_at
    })
  end

  def sync_call_notes(user, notes) do
    Enum.each(notes, fn note_data ->
      call_log = Repo.get(CallLog, note_data["callLogId"])
      
      if call_log && call_log.lead_id do
        Crm.add_note(
          call_log.lead_id,
          note_data["content"],
          user,
          call_log.id
        )
      end
    end)

    {:ok, %{synced: length(notes)}}
  end

  def sync_recordings(user, recordings) do
    Enum.each(recordings, fn rec_data ->
      call_log = Repo.get(CallLog, rec_data["callLogId"])
      
      if call_log do
        # Generate presigned upload URL
        {:ok, upload_url} = generate_upload_url(user, call_log)
        
        call_log
        |> Ecto.Changeset.change(%{recording_status: "pending"})
        |> Repo.update()

        # Return upload URL to client
        {:ok, %{call_log_id: call_log.id, upload_url: upload_url}}
      end
    end)
  end

  def confirm_recording_upload(call_log_id, recording_url) do
    CallLog
    |> Repo.get!(call_log_id)
    |> Ecto.Changeset.change(%{
      recording_url: recording_url,
      recording_status: "uploaded"
    })
    |> Repo.update()
  end

  def get_call_logs(user, params) do
    CallLog
    |> where([c], c.user_id == ^user.id)
    |> apply_call_filters(params)
    |> order_by([c], desc: c.call_at)
    |> preload(:lead)
    |> Repo.all()
  end

  defp generate_upload_url(user, call_log) do
    key = "recordings/#{user.organization_id}/#{user.id}/#{call_log.id}.m4a"
    
    {:ok, ExAws.S3.presigned_url(
      ExAws.Config.new(:s3),
      :put,
      System.get_env("S3_BUCKET"),
      key,
      expires_in: 3600
    )}
  end
end
```

### Day 3-4: Call Controller

```elixir
# lib/edu_consult_crm_web/controllers/api/v1/call_controller.ex
defmodule EduConsultCrmWeb.Api.V1.CallController do
  use EduConsultCrmWeb, :controller

  alias EduConsultCrm.Calls
  alias EduConsultCrm.Accounts.Guardian

  action_fallback EduConsultCrmWeb.FallbackController

  def sync(conn, %{"callLogs" => call_logs}) do
    user = Guardian.Plug.current_resource(conn)
    
    case Calls.sync_call_logs(user, call_logs) do
      {:ok, result} ->
        render(conn, :sync_result, result: result)
      {:error, reason} ->
        conn
        |> put_status(:unprocessable_entity)
        |> render(:error, message: reason)
    end
  end

  def sync_notes(conn, %{"notes" => notes}) do
    user = Guardian.Plug.current_resource(conn)
    
    case Calls.sync_call_notes(user, notes) do
      {:ok, result} ->
        render(conn, :sync_result, result: result)
    end
  end

  def sync_recordings(conn, %{"recordings" => recordings}) do
    user = Guardian.Plug.current_resource(conn)
    
    results = Calls.sync_recordings(user, recordings)
    render(conn, :recording_urls, results: results)
  end

  def details(conn, %{"callLogId" => call_log_id}) do
    call_log = Calls.get_call_log!(call_log_id)
    render(conn, :show, call_log: call_log)
  end

  def available_space(conn, _params) do
    user = Guardian.Plug.current_resource(conn)
    space = Calls.get_available_space(user.organization_id)
    render(conn, :space, available: space)
  end

  def suggest_paths(conn, _params) do
    paths = [
      "/storage/emulated/0/Recordings/Call",
      "/storage/emulated/0/MIUI/sound_recorder/call_rec",
      "/storage/emulated/0/Record/Call"
    ]
    render(conn, :paths, paths: paths)
  end
end
```

---

# PHASE 3: BACKGROUND JOBS & NOTIFICATIONS (Week 6)

## Oban Workers

```elixir
# lib/edu_consult_crm/workers/reminder_worker.ex
defmodule EduConsultCrm.Workers.ReminderWorker do
  use Oban.Worker, queue: :reminders, max_attempts: 3

  alias EduConsultCrm.Crm
  alias EduConsultCrm.Notifications

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "daily_reminders"}}) do
    today = Date.utc_today()
    
    # Get all leads with follow-up today
    leads = Crm.get_leads_with_follow_up_on(today)

    Enum.each(leads, fn lead ->
      if lead.assigned_to do
        Notifications.send_push(
          lead.assigned_to,
          "Follow-up Reminder",
          "Follow up with #{lead.first_name} #{lead.last_name}",
          %{
            type: "FOLLOW_UP_REMINDER",
            lead_id: lead.id
          }
        )
      end
    end)

    :ok
  end

  def perform(%Oban.Job{args: %{"type" => "single", "lead_id" => lead_id}}) do
    lead = Crm.get_lead!(lead_id)
    
    if lead.assigned_to do
      Notifications.send_push(
        lead.assigned_to,
        "Follow-up Reminder",
        "Time to follow up with #{lead.first_name}",
        %{type: "FOLLOW_UP_REMINDER", lead_id: lead_id}
      )
    end

    :ok
  end
end

# lib/edu_consult_crm/workers/sync_worker.ex
defmodule EduConsultCrm.Workers.SyncWorker do
  use Oban.Worker, queue: :sync, max_attempts: 5

  @impl Oban.Worker
  def perform(%Oban.Job{args: %{"type" => "full_sync", "org_id" => org_id}}) do
    # Sync courses from external API
    sync_courses(org_id)
    
    # Sync institutions
    sync_institutions(org_id)
    
    :ok
  end
end

# Schedule daily reminders
# In config/config.exs
config :edu_consult_crm, Oban,
  repo: EduConsultCrm.Repo,
  plugins: [
    {Oban.Plugins.Pruner, max_age: 60 * 60 * 24 * 7},
    {Oban.Plugins.Cron,
      crontab: [
        {"0 8 * * *", EduConsultCrm.Workers.ReminderWorker, args: %{type: "daily_reminders"}},
        {"0 2 * * *", EduConsultCrm.Workers.SyncWorker, args: %{type: "full_sync"}}
      ]}
  ],
  queues: [default: 10, reminders: 5, sync: 3, recordings: 5]
```

## FCM Notifications

```elixir
# lib/edu_consult_crm/notifications/fcm_sender.ex
defmodule EduConsultCrm.Notifications.FCMSender do
  @fcm_url "https://fcm.googleapis.com/fcm/send"

  def send(device_token, title, body, data \\ %{}) do
    headers = [
      {"Authorization", "key=#{fcm_server_key()}"},
      {"Content-Type", "application/json"}
    ]

    payload = %{
      to: device_token,
      notification: %{
        title: title,
        body: body
      },
      data: data,
      priority: "high"
    }

    case Tesla.post(@fcm_url, Jason.encode!(payload), headers: headers) do
      {:ok, %{status: 200}} -> :ok
      {:ok, %{status: status, body: body}} -> {:error, {status, body}}
      {:error, reason} -> {:error, reason}
    end
  end

  defp fcm_server_key, do: System.get_env("FCM_SERVER_KEY")
end

# lib/edu_consult_crm/notifications/notifications.ex
defmodule EduConsultCrm.Notifications do
  alias EduConsultCrm.Accounts
  alias EduConsultCrm.Notifications.FCMSender

  def send_push(user_id, title, body, data \\ %{}) do
    user = Accounts.get_user!(user_id)
    
    if user.fcm_token do
      FCMSender.send(user.fcm_token, title, body, data)
    else
      {:error, :no_fcm_token}
    end
  end

  def notify_lead_assigned(lead, assignee_id) do
    send_push(
      assignee_id,
      "New Lead Assigned",
      "#{lead.first_name} #{lead.last_name} has been assigned to you",
      %{
        type: "LEAD_ASSIGNED",
        lead_id: lead.id
      }
    )
  end

  def notify_new_inquiry(org_id, lead) do
    # Notify all admins/managers
    admins = Accounts.list_admins(org_id)
    
    Enum.each(admins, fn admin ->
      send_push(
        admin.id,
        "New Inquiry",
        "New inquiry from #{lead.first_name}",
        %{type: "NEW_INQUIRY", lead_id: lead.id}
      )
    end)
  end
end
```

---

# PHASE 4: ADMIN DASHBOARD - LIVEVIEW (Week 7)

## Dashboard LiveView

```elixir
# lib/edu_consult_crm_web/live/dashboard_live/index.ex
defmodule EduConsultCrmWeb.Admin.DashboardLive.Index do
  use EduConsultCrmWeb, :live_view

  alias EduConsultCrm.Crm
  alias EduConsultCrm.Calls

  @impl true
  def mount(_params, session, socket) do
    user = get_user_from_session(session)
    org_id = user.organization_id

    if connected?(socket) do
      # Subscribe to real-time updates
      Phoenix.PubSub.subscribe(EduConsultCrm.PubSub, "org:#{org_id}:leads")
      Phoenix.PubSub.subscribe(EduConsultCrm.PubSub, "org:#{org_id}:calls")
      
      # Refresh stats every minute
      :timer.send_interval(60_000, self(), :refresh_stats)
    end

    socket =
      socket
      |> assign(:user, user)
      |> assign(:org_id, org_id)
      |> assign_stats()

    {:ok, socket}
  end

  @impl true
  def handle_info(:refresh_stats, socket) do
    {:noreply, assign_stats(socket)}
  end

  def handle_info({:lead_created, _lead}, socket) do
    {:noreply, assign_stats(socket)}
  end

  def handle_info({:lead_updated, _lead}, socket) do
    {:noreply, assign_stats(socket)}
  end

  defp assign_stats(socket) do
    org_id = socket.assigns.org_id

    socket
    |> assign(:lead_stats, Crm.get_lead_stats(org_id))
    |> assign(:leads_by_status, Crm.get_leads_by_status(org_id))
    |> assign(:leads_by_source, Crm.get_leads_by_source(org_id))
    |> assign(:call_stats, Calls.get_call_stats(org_id, Date.utc_today()))
    |> assign(:recent_activities, Crm.get_recent_activities(org_id, 10))
    |> assign(:top_performers, Crm.get_top_performers(org_id))
  end

  @impl true
  def render(assigns) do
    ~H"""
    <div class="dashboard">
      <h1 class="text-2xl font-bold mb-6">Dashboard</h1>

      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <.stat_card 
          title="Total Leads" 
          value={@lead_stats.total} 
          icon="users" 
        />
        <.stat_card 
          title="New Today" 
          value={@lead_stats.new_today} 
          icon="user-plus"
          color="green" 
        />
        <.stat_card 
          title="Pending Follow-ups" 
          value={@lead_stats.pending_follow_ups} 
          icon="clock"
          color="yellow" 
        />
        <.stat_card 
          title="Overdue" 
          value={@lead_stats.overdue_follow_ups} 
          icon="alert-circle"
          color="red" 
        />
      </div>

      <!-- Charts Row -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        <!-- Pipeline Chart -->
        <div class="bg-white rounded-lg shadow p-4">
          <h2 class="text-lg font-semibold mb-4">Lead Pipeline</h2>
          <div id="pipeline-chart" phx-hook="PipelineChart" data-stats={Jason.encode!(@leads_by_status)}>
          </div>
        </div>

        <!-- Sources Chart -->
        <div class="bg-white rounded-lg shadow p-4">
          <h2 class="text-lg font-semibold mb-4">Lead Sources</h2>
          <div id="sources-chart" phx-hook="SourcesChart" data-stats={Jason.encode!(@leads_by_source)}>
          </div>
        </div>
      </div>

      <!-- Recent Activities -->
      <div class="bg-white rounded-lg shadow p-4">
        <h2 class="text-lg font-semibold mb-4">Recent Activities</h2>
        <div class="space-y-3">
          <%= for activity <- @recent_activities do %>
            <.activity_item activity={activity} />
          <% end %>
        </div>
      </div>
    </div>
    """
  end

  defp stat_card(assigns) do
    ~H"""
    <div class={"bg-white rounded-lg shadow p-4 border-l-4 border-#{@color || "blue"}-500"}>
      <div class="flex items-center justify-between">
        <div>
          <p class="text-gray-500 text-sm"><%= @title %></p>
          <p class="text-2xl font-bold"><%= @value %></p>
        </div>
        <div class={"text-#{@color || "blue"}-500"}>
          <.icon name={@icon} class="w-8 h-8" />
        </div>
      </div>
    </div>
    """
  end
end
```

---

# DEPLOYMENT

## Docker Configuration

```dockerfile
# Dockerfile
FROM elixir:1.16-alpine AS builder

RUN apk add --no-cache build-base git

WORKDIR /app

ENV MIX_ENV=prod

COPY mix.exs mix.lock ./
RUN mix deps.get --only prod
RUN mix deps.compile

COPY . .
RUN mix compile
RUN mix assets.deploy
RUN mix release

# Runtime
FROM alpine:3.18 AS runner

RUN apk add --no-cache libstdc++ openssl ncurses-libs

WORKDIR /app

COPY --from=builder /app/_build/prod/rel/edu_consult_crm ./

ENV HOME=/app
ENV MIX_ENV=prod
ENV PHX_SERVER=true

EXPOSE 4000

CMD ["bin/edu_consult_crm", "start"]
```

## Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "4000:4000"
    environment:
      - DATABASE_URL=ecto://postgres:postgres@db/edu_consult_crm_prod
      - SECRET_KEY_BASE=${SECRET_KEY_BASE}
      - PHX_HOST=${PHX_HOST}
      - FCM_SERVER_KEY=${FCM_SERVER_KEY}
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
      - S3_BUCKET=${S3_BUCKET}
    depends_on:
      - db
      - redis

  db:
    image: postgres:15-alpine
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
      - POSTGRES_DB=edu_consult_crm_prod

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

---

# TIMELINE SUMMARY

| Week | Focus | Deliverables |
|------|-------|--------------|
| 1 | Project Setup | Phoenix app, migrations, core contexts |
| 2 | Authentication | Guardian, JWT, login/register APIs |
| 3 | Lead Management | CRUD, search, filters, statuses |
| 4 | Call Tracking | Sync, notes, recordings |
| 5 | Courses & Templates | Course/institution CRUD, templates |
| 6 | Background Jobs | Oban workers, FCM notifications |
| 7 | Admin Dashboard | LiveView dashboard, reports |
| 8 | Testing & Deploy | Tests, Docker, deployment |

---

# API REFERENCE

## Authentication
```
POST /api/v1/oauth/token          - Login
POST /api/v1/auth/refresh         - Refresh token
POST /api/v1/auth/register        - Register
POST /api/v1/auth/register/sendOTP    - Send OTP
POST /api/v1/auth/register/verifyOTP  - Verify OTP
```

## Leads
```
POST /api/v1/lead/getData         - List leads
POST /api/v1/lead/save            - Create/Update lead
POST /api/v1/lead/saveNote        - Add note
POST /api/v1/lead/status          - List statuses
POST /api/v1/lead/getByNumber     - Find by phone
POST /api/v1/lead/allTags         - List tags
```

## Calls
```
POST /api/v1/callLog/sync         - Sync call logs
POST /api/v1/callLog/sync/note    - Sync notes
POST /api/v1/callRecording/sync   - Sync recordings
```

## Templates
```
POST /api/v1/callnote/template/fetchAll   - List note templates
POST /api/v1/messagetemplate/fetchAll     - List message templates
```

---

*Document Version: 1.0*  
*Stack: Elixir 1.16 + Phoenix 1.7 + PostgreSQL 15*
