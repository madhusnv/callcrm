# Script for populating the database.
#
# Run with: mix run priv/repo/seeds.exs

alias EduConsultCrm.Repo
alias EduConsultCrm.Tenants.Organization
alias EduConsultCrm.Tenants.Branch
alias EduConsultCrm.Accounts.User
alias EduConsultCrm.Crm.LeadStatus

# Create a demo organization
{:ok, org} =
  %Organization{}
  |> Organization.changeset(%{
    name: "Demo Educational Consultancy",
    slug: "demo-edu"
  })
  |> Repo.insert()

IO.puts("Created organization: #{org.name}")
IO.puts("API Key: #{org.api_key}")

# Create a branch
{:ok, branch} =
  %Branch{}
  |> Branch.changeset(%{
    name: "Main Branch",
    code: "MAIN",
    organization_id: org.id
  })
  |> Repo.insert()

IO.puts("Created branch: #{branch.name}")

# Create an admin user
{:ok, admin} =
  %User{organization_id: org.id, branch_id: branch.id}
  |> User.registration_changeset(%{
    email: "admin@demo.com",
    phone: "9876543210",
    password: "password123",
    first_name: "Admin",
    last_name: "User",
    role: "admin"
  })
  |> Repo.insert()

IO.puts("Created admin user: #{admin.email}")

# Create default lead statuses
statuses = [
  %{name: "New", code: "NEW", color: "#2196F3", order: 1, is_default: true},
  %{name: "Contacted", code: "CONTACTED", color: "#FF9800", order: 2},
  %{name: "Interested", code: "INTERESTED", color: "#4CAF50", order: 3},
  %{name: "Follow Up", code: "FOLLOWUP", color: "#9C27B0", order: 4},
  %{name: "Converted", code: "CONVERTED", color: "#00BCD4", order: 5, is_closed: true},
  %{name: "Not Interested", code: "NOT_INTERESTED", color: "#F44336", order: 6, is_closed: true}
]

for status_attrs <- statuses do
  {:ok, status} =
    %LeadStatus{}
    |> LeadStatus.changeset(Map.put(status_attrs, :organization_id, org.id))
    |> Repo.insert()

  IO.puts("Created status: #{status.name}")
end

IO.puts("\n=== Seed Complete ===")
IO.puts("Organization: #{org.name}")
IO.puts("API Key: #{org.api_key}")
IO.puts("Admin Email: admin@demo.com")
IO.puts("Admin Password: password123")
