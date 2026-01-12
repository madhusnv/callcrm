defmodule EduConsultCrm.Repo.Migrations.CreateMessageTemplates do
  use Ecto.Migration

  def change do
    create table(:message_templates, primary_key: false) do
      add :id, :binary_id, primary_key: true

      add :organization_id, references(:organizations, type: :binary_id, on_delete: :delete_all),
        null: false

      add :name, :string, null: false
      add :content, :text, null: false
      # greeting, follow_up, offer, visa_info, general
      add :category, :string
      # e.g., ["{{name}}", "{{course}}"]
      add :dynamic_fields, {:array, :string}, default: []
      add :whatsapp_enabled, :boolean, default: true
      add :sms_enabled, :boolean, default: true
      add :order, :integer, default: 0
      add :is_active, :boolean, default: true

      timestamps(type: :utc_datetime)
    end

    create index(:message_templates, [:organization_id])
    create index(:message_templates, [:category])
    create index(:message_templates, [:is_active])
  end
end
