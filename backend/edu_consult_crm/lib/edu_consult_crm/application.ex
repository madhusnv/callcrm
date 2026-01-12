defmodule EduConsultCrm.Application do
  # See https://hexdocs.pm/elixir/Application.html
  # for more information on OTP Applications
  @moduledoc false

  use Application

  @impl true
  def start(_type, _args) do
    children = [
      EduConsultCrmWeb.Telemetry,
      EduConsultCrm.Repo,
      {DNSCluster, query: Application.get_env(:edu_consult_crm, :dns_cluster_query) || :ignore},
      {Phoenix.PubSub, name: EduConsultCrm.PubSub},
      # Oban background job processor
      {Oban, Application.fetch_env!(:edu_consult_crm, Oban)},
      # Start to serve requests, typically the last entry
      EduConsultCrmWeb.Endpoint
    ]

    # See https://hexdocs.pm/elixir/Supervisor.html
    # for other strategies and supported options
    opts = [strategy: :one_for_one, name: EduConsultCrm.Supervisor]
    Supervisor.start_link(children, opts)
  end

  # Tell Phoenix to update the endpoint configuration
  # whenever the application is updated.
  @impl true
  def config_change(changed, _new, removed) do
    EduConsultCrmWeb.Endpoint.config_change(changed, removed)
    :ok
  end
end
