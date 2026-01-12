defmodule EduConsultCrm.Accounts.AuthPipeline do
  use Guardian.Plug.Pipeline,
    otp_app: :edu_consult_crm,
    module: EduConsultCrm.Accounts.Guardian,
    error_handler: EduConsultCrm.Accounts.AuthErrorHandler

  plug Guardian.Plug.VerifyHeader, scheme: "Bearer"
  plug Guardian.Plug.EnsureAuthenticated
  plug Guardian.Plug.LoadResource
end
