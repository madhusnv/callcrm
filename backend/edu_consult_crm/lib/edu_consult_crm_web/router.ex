defmodule EduConsultCrmWeb.Router do
  use EduConsultCrmWeb, :router

  pipeline :browser do
    plug :accepts, ["html"]
    plug :fetch_session
    plug :fetch_live_flash
    plug :put_root_layout, html: {EduConsultCrmWeb.Layouts, :root}
    plug :protect_from_forgery
    plug :put_secure_browser_headers
  end

  pipeline :api do
    plug :accepts, ["json"]
    plug CORSPlug
  end

  pipeline :api_tenant do
    plug EduConsultCrmWeb.Plugs.TenantPlug
  end

  pipeline :api_authenticated do
    plug EduConsultCrm.Accounts.AuthPipeline
    plug EduConsultCrmWeb.Plugs.EnsureUserOrgMatch
  end

  pipeline :api_rate_limited do
    plug EduConsultCrmWeb.Plugs.RateLimitPlug, max_requests: 10, window_ms: 60_000
  end

  pipeline :api_auth_rate_limited do
    plug EduConsultCrmWeb.Plugs.RateLimitPlug, max_requests: 5, window_ms: 60_000
  end

  pipeline :admin do
    plug EduConsultCrmWeb.Plugs.AdminAuthPlug
  end

  # Browser routes
  scope "/", EduConsultCrmWeb do
    pipe_through :browser

    get "/", PageController, :home
  end

  # LiveView admin
  scope "/admin", EduConsultCrmWeb.Admin do
    pipe_through [:browser, :admin]

    live "/dashboard", DashboardLive
    live "/leads", LeadLive.Index
    live "/reports", ReportsLive
  end

  # Public API routes (require API key for tenant resolution)
  scope "/api/v1", EduConsultCrmWeb.Api.V1 do
    pipe_through [:api, :api_tenant]

    post "/oauth/token", AuthController, :login
    post "/auth/refresh", AuthController, :refresh
    post "/auth/register", AuthController, :register
    post "/auth/register/sendOTP", AuthController, :send_otp
    post "/auth/register/verifyOTP", AuthController, :verify_otp
  end

  # Protected API routes (require API key + auth token)
  scope "/api/v1", EduConsultCrmWeb.Api.V1 do
    pipe_through [:api, :api_tenant, :api_authenticated]

    post "/auth/logout", AuthController, :logout

    # Employee endpoints
    post "/employee/updateFCM", EmployeeController, :update_fcm
    # post "/employee/settings/save", EmployeeController, :save_settings

    # Lead endpoints
    post "/lead/getData", LeadController, :index
    post "/lead/save", LeadController, :create_or_update
    post "/lead/saveNote", LeadController, :save_note
    post "/lead/status", LeadController, :list_statuses
    post "/lead/getByNumber", LeadController, :get_by_number
    post "/lead/getById", LeadController, :get_by_id
    post "/lead/note", LeadController, :get_notes
    post "/lead/allTags", LeadController, :all_tags
    post "/lead/notContacted", LeadController, :not_contacted
    post "/lead/isDeleted", LeadController, :check_deleted
    post "/lead/restore", LeadController, :restore
    post "/lead/callBack/totalDue", LeadController, :due_callbacks

    # Call Log endpoints
    post "/callLog/sync", CallLogController, :sync
    post "/callLog/sync/note", CallLogController, :sync_notes
    post "/callLog/getByLead", CallLogController, :get_by_lead

    # Recording endpoints
    post "/callRecording/getUploadUrl", CallLogController, :upload_recording
    post "/callRecording/confirmUpload", CallLogController, :confirm_upload
    get "/callRecording/stream/:id", CallLogController, :stream_recording

    # Template endpoints
    post "/callnote/template/fetchAll", TemplateController, :list_note_templates
    post "/messagetemplate/fetchAll", TemplateController, :list_message_templates
    post "/messagetemplate/render", TemplateController, :render_template

    # Education endpoints
    post "/education/countries", EducationController, :list_countries
    post "/education/institutions", EducationController, :list_institutions
    post "/education/courses", EducationController, :list_courses
    post "/education/course/get", EducationController, :get_course

    # Dashboard endpoints
    post "/dashboard/stats", DashboardController, :stats
  end

  # Enable LiveDashboard and Swoosh mailbox preview in development
  if Application.compile_env(:edu_consult_crm, :dev_routes) do
    import Phoenix.LiveDashboard.Router

    scope "/dev" do
      pipe_through :browser

      live_dashboard "/dashboard", metrics: EduConsultCrmWeb.Telemetry
      forward "/mailbox", Plug.Swoosh.MailboxPreview
    end
  end
end
