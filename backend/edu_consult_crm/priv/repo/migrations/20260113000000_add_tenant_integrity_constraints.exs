defmodule EduConsultCrm.Repo.Migrations.AddTenantIntegrityConstraints do
  use Ecto.Migration

  def up do
    alter table(:lead_notes) do
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all)
    end

    alter table(:lead_tags) do
      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all)
    end

    execute """
    UPDATE lead_notes n
    SET organization_id = l.organization_id
    FROM leads l
    WHERE n.lead_id = l.id AND n.organization_id IS NULL;
    """

    execute """
    UPDATE lead_tags lt
    SET organization_id = l.organization_id
    FROM leads l
    WHERE lt.lead_id = l.id AND lt.organization_id IS NULL;
    """

    execute """
    DELETE FROM lead_tags lt
    USING leads l, tags t
    WHERE lt.lead_id = l.id
      AND lt.tag_id = t.id
      AND l.organization_id <> t.organization_id;
    """

    alter table(:lead_notes) do
      modify :organization_id, :binary_id, null: false
    end

    alter table(:lead_tags) do
      modify :organization_id, :binary_id, null: false
    end

    create index(:lead_notes, [:organization_id])
    create index(:lead_tags, [:organization_id])

    create unique_index(:users, [:id, :organization_id], name: :users_org_id_unique)
    create unique_index(:branches, [:id, :organization_id], name: :branches_org_id_unique)

    create unique_index(:lead_statuses, [:id, :organization_id],
             name: :lead_statuses_org_id_unique
           )

    create unique_index(:leads, [:id, :organization_id], name: :leads_org_id_unique)
    create unique_index(:tags, [:id, :organization_id], name: :tags_org_id_unique)
    create unique_index(:call_logs, [:id, :organization_id], name: :call_logs_org_id_unique)

    create unique_index(:call_recordings, [:id, :organization_id],
             name: :call_recordings_org_id_unique
           )

    execute """
    ALTER TABLE leads
    ADD CONSTRAINT leads_status_org_fkey
    FOREIGN KEY (organization_id, status_id)
    REFERENCES lead_statuses (organization_id, id)
    ON DELETE RESTRICT;
    """

    execute """
    ALTER TABLE leads
    ADD CONSTRAINT leads_branch_org_fkey
    FOREIGN KEY (organization_id, branch_id)
    REFERENCES branches (organization_id, id)
    ON DELETE SET NULL;
    """

    execute """
    ALTER TABLE leads
    ADD CONSTRAINT leads_assigned_org_fkey
    FOREIGN KEY (organization_id, assigned_to)
    REFERENCES users (organization_id, id)
    ON DELETE SET NULL;
    """

    execute """
    ALTER TABLE leads
    ADD CONSTRAINT leads_created_by_org_fkey
    FOREIGN KEY (organization_id, created_by)
    REFERENCES users (organization_id, id)
    ON DELETE SET NULL;
    """

    execute """
    ALTER TABLE call_logs
    ADD CONSTRAINT call_logs_lead_org_fkey
    FOREIGN KEY (organization_id, lead_id)
    REFERENCES leads (organization_id, id)
    ON DELETE SET NULL;
    """

    execute """
    ALTER TABLE call_recordings
    ADD CONSTRAINT call_recordings_call_log_org_fkey
    FOREIGN KEY (organization_id, call_log_id)
    REFERENCES call_logs (organization_id, id)
    ON DELETE CASCADE;
    """

    execute """
    ALTER TABLE lead_notes
    ADD CONSTRAINT lead_notes_lead_org_fkey
    FOREIGN KEY (organization_id, lead_id)
    REFERENCES leads (organization_id, id)
    ON DELETE CASCADE;
    """

    execute """
    ALTER TABLE lead_notes
    ADD CONSTRAINT lead_notes_user_org_fkey
    FOREIGN KEY (organization_id, user_id)
    REFERENCES users (organization_id, id)
    ON DELETE SET NULL;
    """

    execute """
    ALTER TABLE lead_tags
    ADD CONSTRAINT lead_tags_lead_org_fkey
    FOREIGN KEY (organization_id, lead_id)
    REFERENCES leads (organization_id, id)
    ON DELETE CASCADE;
    """

    execute """
    ALTER TABLE lead_tags
    ADD CONSTRAINT lead_tags_tag_org_fkey
    FOREIGN KEY (organization_id, tag_id)
    REFERENCES tags (organization_id, id)
    ON DELETE CASCADE;
    """
  end

  def down do
    execute "ALTER TABLE lead_tags DROP CONSTRAINT IF EXISTS lead_tags_tag_org_fkey;"
    execute "ALTER TABLE lead_tags DROP CONSTRAINT IF EXISTS lead_tags_lead_org_fkey;"
    execute "ALTER TABLE lead_notes DROP CONSTRAINT IF EXISTS lead_notes_user_org_fkey;"
    execute "ALTER TABLE lead_notes DROP CONSTRAINT IF EXISTS lead_notes_lead_org_fkey;"

    execute "ALTER TABLE call_recordings DROP CONSTRAINT IF EXISTS call_recordings_call_log_org_fkey;"

    execute "ALTER TABLE call_logs DROP CONSTRAINT IF EXISTS call_logs_lead_org_fkey;"
    execute "ALTER TABLE leads DROP CONSTRAINT IF EXISTS leads_created_by_org_fkey;"
    execute "ALTER TABLE leads DROP CONSTRAINT IF EXISTS leads_assigned_org_fkey;"
    execute "ALTER TABLE leads DROP CONSTRAINT IF EXISTS leads_branch_org_fkey;"
    execute "ALTER TABLE leads DROP CONSTRAINT IF EXISTS leads_status_org_fkey;"

    drop_if_exists unique_index(:call_recordings, [:id, :organization_id],
                     name: :call_recordings_org_id_unique
                   )

    drop_if_exists unique_index(:call_logs, [:id, :organization_id],
                     name: :call_logs_org_id_unique
                   )

    drop_if_exists unique_index(:tags, [:id, :organization_id], name: :tags_org_id_unique)
    drop_if_exists unique_index(:leads, [:id, :organization_id], name: :leads_org_id_unique)

    drop_if_exists unique_index(:lead_statuses, [:id, :organization_id],
                     name: :lead_statuses_org_id_unique
                   )

    drop_if_exists unique_index(:branches, [:id, :organization_id], name: :branches_org_id_unique)
    drop_if_exists unique_index(:users, [:id, :organization_id], name: :users_org_id_unique)

    drop_if_exists index(:lead_tags, [:organization_id])
    drop_if_exists index(:lead_notes, [:organization_id])

    alter table(:lead_tags) do
      remove :organization_id
    end

    alter table(:lead_notes) do
      remove :organization_id
    end
  end
end
