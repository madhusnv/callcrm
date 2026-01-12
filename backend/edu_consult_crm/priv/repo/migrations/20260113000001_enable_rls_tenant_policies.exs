defmodule EduConsultCrm.Repo.Migrations.EnableRlsTenantPolicies do
  use Ecto.Migration

  @tables_with_org [
    :users,
    :branches,
    :lead_statuses,
    :leads,
    :lead_notes,
    :lead_activities,
    :tags,
    :lead_tags,
    :call_logs,
    :call_recordings,
    :note_templates,
    :message_templates,
    :countries,
    :institutions,
    :courses
  ]

  def up do
    Enum.each(@tables_with_org, fn table ->
      execute("ALTER TABLE #{table} ENABLE ROW LEVEL SECURITY")
      execute("ALTER TABLE #{table} FORCE ROW LEVEL SECURITY")

      execute("""
      CREATE POLICY #{table}_tenant_isolation
      ON #{table}
      USING (
        current_setting('app.bypass_rls', true) = 'on'
        OR organization_id = current_setting('app.current_org', true)::uuid
      )
      WITH CHECK (
        current_setting('app.bypass_rls', true) = 'on'
        OR organization_id = current_setting('app.current_org', true)::uuid
      )
      """)
    end)
  end

  def down do
    Enum.each(@tables_with_org, fn table ->
      execute("DROP POLICY IF EXISTS #{table}_tenant_isolation ON #{table}")
      execute("ALTER TABLE #{table} DISABLE ROW LEVEL SECURITY")
    end)
  end
end
