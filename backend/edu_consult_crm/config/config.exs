# This file is responsible for configuring your application
# and its dependencies with the aid of the Config module.
#
# This configuration file is loaded before any dependency and
# is restricted to this project.

# General application configuration
import Config

config :edu_consult_crm,
  ecto_repos: [EduConsultCrm.Repo],
  generators: [timestamp_type: :utc_datetime]

# Configure the endpoint
config :edu_consult_crm, EduConsultCrmWeb.Endpoint,
  url: [host: "localhost"],
  adapter: Bandit.PhoenixAdapter,
  render_errors: [
    formats: [html: EduConsultCrmWeb.ErrorHTML, json: EduConsultCrmWeb.ErrorJSON],
    layout: false
  ],
  pubsub_server: EduConsultCrm.PubSub,
  live_view: [signing_salt: "0D2mZjLm"]

# Configure the mailer
#
# By default it uses the "Local" adapter which stores the emails
# locally. You can see the emails in your browser, at "/dev/mailbox".
#
# For production it's recommended to configure a different adapter
# at the `config/runtime.exs`.
config :edu_consult_crm, EduConsultCrm.Mailer, adapter: Swoosh.Adapters.Local

# Configure esbuild (the version is required)
config :esbuild,
  version: "0.25.4",
  edu_consult_crm: [
    args:
      ~w(js/app.js --bundle --target=es2022 --outdir=../priv/static/assets/js --external:/fonts/* --external:/images/* --alias:@=.),
    cd: Path.expand("../assets", __DIR__),
    env: %{"NODE_PATH" => [Path.expand("../deps", __DIR__), Mix.Project.build_path()]}
  ]

# Configure tailwind (the version is required)
config :tailwind,
  version: "4.1.12",
  edu_consult_crm: [
    args: ~w(
      --input=assets/css/app.css
      --output=priv/static/assets/css/app.css
    ),
    cd: Path.expand("..", __DIR__)
  ]

# Configure Elixir's Logger
config :logger, :default_formatter,
  format: "$time $metadata[$level] $message\n",
  metadata: [:request_id]

# Use Jason for JSON parsing in Phoenix
config :phoenix, :json_library, Jason

# Guardian JWT configuration
config :edu_consult_crm, EduConsultCrm.Accounts.Guardian,
  issuer: "edu_consult_crm",
  secret_key: "dev-secret-key-change-in-production-use-mix-guardian-gen-secret"

# Oban background jobs
config :edu_consult_crm, Oban,
  repo: EduConsultCrm.Repo,
  plugins: [
    {Oban.Plugins.Cron,
     crontab: [
       {"0 9 * * *", EduConsultCrm.Workers.ReminderWorker,
        args: %{"type" => "check_due_followups_all"}}
     ]},
    Oban.Plugins.Pruner
  ],
  queues: [default: 10, sync: 5, notifications: 5, reminders: 5]

# Import environment specific config. This must remain at the bottom
# of this file so it overrides the configuration defined above.
import_config "#{config_env()}.exs"
