# Script for populating the database.
#
# Run with: mix run priv/repo/seeds.exs

alias EduConsultCrm.Repo
alias EduConsultCrm.Tenants.Organization
alias EduConsultCrm.Tenants.Branch
alias EduConsultCrm.Accounts.User
alias EduConsultCrm.Crm.LeadStatus
alias EduConsultCrm.Education.Country
alias EduConsultCrm.Education.Institution
alias EduConsultCrm.Education.Course

org =
  case Repo.get_by(Organization, slug: "demo-edu") do
    nil ->
      {:ok, created} =
        %Organization{}
        |> Organization.changeset(%{
          name: "Demo Educational Consultancy",
          slug: "demo-edu"
        })
        |> Repo.insert()

      created

    existing ->
      existing
  end

IO.puts("Created organization: #{org.name}")
IO.puts("API Key: #{org.api_key}")

branch =
  case Repo.get_by(Branch, organization_id: org.id, code: "MAIN") do
    nil ->
      {:ok, created} =
        %Branch{}
        |> Branch.changeset(%{
          name: "Main Branch",
          code: "MAIN",
          organization_id: org.id
        })
        |> Repo.insert()

      created

    existing ->
      existing
  end

IO.puts("Created branch: #{branch.name}")

admin =
  case Repo.get_by(User, organization_id: org.id, email: "admin@demo.com") do
    nil ->
      {:ok, created} =
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

      created

    existing ->
      existing
  end

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
  status =
    case Repo.get_by(LeadStatus, organization_id: org.id, code: status_attrs.code) do
      nil ->
        {:ok, created} =
          %LeadStatus{}
          |> LeadStatus.changeset(Map.put(status_attrs, :organization_id, org.id))
          |> Repo.insert()

        created

      existing ->
        existing
    end

  IO.puts("Created status: #{status.name}")
end

countries = [
  %{name: "India", code: "IN", currency_code: "INR", phone_code: "+91", display_order: 1},
  %{name: "United States", code: "US", currency_code: "USD", phone_code: "+1", display_order: 2},
  %{name: "Canada", code: "CA", currency_code: "CAD", phone_code: "+1", display_order: 3},
  %{
    name: "United Kingdom",
    code: "GB",
    currency_code: "GBP",
    phone_code: "+44",
    display_order: 4
  },
  %{name: "Australia", code: "AU", currency_code: "AUD", phone_code: "+61", display_order: 5}
]

country_map =
  for attrs <- countries, into: %{} do
    country =
      case Repo.get_by(Country, organization_id: org.id, code: attrs.code) do
        nil ->
          {:ok, created} =
            %Country{organization_id: org.id}
            |> Country.changeset(attrs)
            |> Repo.insert()

          created

        existing ->
          existing
      end

    IO.puts("Created country: #{country.name}")
    {country.code, country}
  end

institutions = [
  %{
    name: "University of Melbourne",
    country_code: "AU",
    city: "Melbourne",
    institution_type: "University"
  },
  %{
    name: "University of Toronto",
    country_code: "CA",
    city: "Toronto",
    institution_type: "University"
  },
  %{
    name: "University of Oxford",
    country_code: "GB",
    city: "Oxford",
    institution_type: "University"
  },
  %{name: "IIT Delhi", country_code: "IN", city: "New Delhi", institution_type: "University"},
  %{
    name: "Stanford University",
    country_code: "US",
    city: "Stanford",
    institution_type: "University"
  }
]

institution_map =
  for attrs <- institutions, into: %{} do
    country = Map.fetch!(country_map, attrs.country_code)

    institution =
      case Repo.get_by(Institution, organization_id: org.id, name: attrs.name) do
        nil ->
          {:ok, created} =
            %Institution{organization_id: org.id}
            |> Institution.changeset(%{
              name: attrs.name,
              city: attrs.city,
              institution_type: attrs.institution_type,
              country_id: country.id
            })
            |> Repo.insert()

          created

        existing ->
          existing
      end

    IO.puts("Created institution: #{institution.name}")
    {attrs.name, institution}
  end

courses = [
  %{
    name: "MBA",
    level: "masters",
    duration_months: 24,
    intake_months: ["September", "January"],
    tuition_fee: Decimal.new("52000"),
    currency_code: "USD",
    country_code: "US",
    institution_name: "Stanford University"
  },
  %{
    name: "Computer Science",
    level: "bachelors",
    duration_months: 48,
    intake_months: ["September"],
    tuition_fee: Decimal.new("35000"),
    currency_code: "CAD",
    country_code: "CA",
    institution_name: "University of Toronto"
  },
  %{
    name: "Data Science",
    level: "masters",
    duration_months: 24,
    intake_months: ["October"],
    tuition_fee: Decimal.new("42000"),
    currency_code: "GBP",
    country_code: "GB",
    institution_name: "University of Oxford"
  }
]

for attrs <- courses do
  country = Map.fetch!(country_map, attrs.country_code)
  institution = Map.fetch!(institution_map, attrs.institution_name)

  course =
    case Repo.get_by(Course,
           organization_id: org.id,
           institution_id: institution.id,
           name: attrs.name
         ) do
      nil ->
        {:ok, created} =
          %Course{organization_id: org.id}
          |> Course.changeset(%{
            name: attrs.name,
            level: attrs.level,
            duration_months: attrs.duration_months,
            intake_months: attrs.intake_months,
            tuition_fee: attrs.tuition_fee,
            currency_code: attrs.currency_code,
            country_id: country.id,
            institution_id: institution.id
          })
          |> Repo.insert()

        created

      existing ->
        existing
    end

  IO.puts("Created course: #{course.name}")
end

IO.puts("\n=== Seed Complete ===")
IO.puts("Organization: #{org.name}")
IO.puts("API Key: #{org.api_key}")
IO.puts("Admin Email: admin@demo.com")
IO.puts("Admin Password: password123")
