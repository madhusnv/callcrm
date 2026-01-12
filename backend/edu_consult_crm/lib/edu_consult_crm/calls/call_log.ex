defmodule EduConsultCrm.Calls.CallLog do
  @moduledoc """
  Schema for call logs synced from mobile devices.
  """
  use Ecto.Schema
  import Ecto.Changeset

  @primary_key {:id, :binary_id, autogenerate: true}
  @foreign_key_type :binary_id

  @call_types ~w(incoming outgoing missed)

  schema "call_logs" do
    belongs_to :organization, EduConsultCrm.Tenants.Organization
    belongs_to :user, EduConsultCrm.Accounts.User
    belongs_to :lead, EduConsultCrm.Crm.Lead

    has_one :recording, EduConsultCrm.Calls.CallRecording

    field :phone_number, :string
    field :call_type, :string
    field :duration, :integer, default: 0
    field :call_at, :utc_datetime
    field :sim_slot, :integer
    field :device_call_id, :string
    field :contact_name, :string
    field :notes, :string

    timestamps(type: :utc_datetime)
  end

  @required_fields ~w(phone_number call_type call_at organization_id user_id)a
  @optional_fields ~w(lead_id duration sim_slot device_call_id contact_name notes)a

  def changeset(call_log, attrs) do
    call_log
    |> cast(attrs, @required_fields ++ @optional_fields)
    |> validate_required(@required_fields)
    |> validate_inclusion(:call_type, @call_types)
    |> validate_number(:duration, greater_than_or_equal_to: 0)
    |> foreign_key_constraint(:organization_id)
    |> foreign_key_constraint(:user_id)
    |> foreign_key_constraint(:lead_id)
    |> unique_constraint([:user_id, :device_call_id], name: :call_logs_user_device_unique)
  end

  def update_changeset(call_log, attrs) do
    call_log
    |> cast(attrs, [:lead_id, :notes, :contact_name])
    |> foreign_key_constraint(:lead_id)
  end

  def call_types, do: @call_types
end
