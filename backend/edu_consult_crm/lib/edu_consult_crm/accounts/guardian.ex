defmodule EduConsultCrm.Accounts.Guardian do
  use Guardian, otp_app: :edu_consult_crm

  alias EduConsultCrm.Accounts

  def subject_for_token(user, _claims) do
    {:ok, to_string(user.id)}
  end

  def resource_from_claims(%{"sub" => id, "org_id" => org_id}) do
    case Accounts.get_user(id) do
      %{organization_id: ^org_id, is_active: true} = user -> {:ok, user}
      _ -> {:error, :resource_not_found}
    end
  end

  def resource_from_claims(_claims), do: {:error, :resource_not_found}
end
