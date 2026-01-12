defmodule EduConsultCrm.Repo do
  use Ecto.Repo,
    otp_app: :edu_consult_crm,
    adapter: Ecto.Adapters.Postgres
end
