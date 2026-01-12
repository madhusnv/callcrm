defmodule EduConsultCrmWeb.Plugs.RateLimitPlug do
  @moduledoc """
  Simple rate limiting plug using ETS for storage.

  For production, consider using Redis-backed solutions like:
  - Hammer (https://github.com/ExHammer/hammer)
  - ExRated (https://github.com/grempe/ex_rated)
  """

  import Plug.Conn
  use GenServer

  @table_name :rate_limit_buckets
  @default_max_requests 10
  @default_window_ms 60_000

  # Client API

  def init(opts) do
    %{
      max_requests: Keyword.get(opts, :max_requests, @default_max_requests),
      window_ms: Keyword.get(opts, :window_ms, @default_window_ms),
      key_func: Keyword.get(opts, :key_func, &default_key/1)
    }
  end

  def call(conn, opts) do
    ensure_table_exists()

    key = opts.key_func.(conn)
    now = System.system_time(:millisecond)
    window_start = now - opts.window_ms

    case check_rate(key, window_start, opts.max_requests, now) do
      :ok ->
        conn

      :rate_limited ->
        send_rate_limited(conn)
    end
  end

  defp ensure_table_exists do
    case :ets.whereis(@table_name) do
      :undefined ->
        :ets.new(@table_name, [:set, :public, :named_table, read_concurrency: true])

      _ ->
        :ok
    end
  end

  defp check_rate(key, window_start, max_requests, now) do
    case :ets.lookup(@table_name, key) do
      [] ->
        :ets.insert(@table_name, {key, [{now, 1}]})
        :ok

      [{^key, requests}] ->
        valid_requests = Enum.filter(requests, fn {ts, _} -> ts > window_start end)
        total = Enum.reduce(valid_requests, 0, fn {_, count}, acc -> acc + count end)

        if total >= max_requests do
          :rate_limited
        else
          updated = [{now, 1} | valid_requests]
          :ets.insert(@table_name, {key, updated})
          :ok
        end
    end
  end

  defp default_key(conn) do
    ip = get_client_ip(conn)
    path = conn.request_path
    "#{ip}:#{path}"
  end

  defp get_client_ip(conn) do
    forwarded_for = get_req_header(conn, "x-forwarded-for")

    case forwarded_for do
      [ip | _] -> ip |> String.split(",") |> List.first() |> String.trim()
      [] -> conn.remote_ip |> Tuple.to_list() |> Enum.join(".")
    end
  end

  defp send_rate_limited(conn) do
    body =
      Jason.encode!(%{
        status: false,
        message: "Too many requests. Please try again later."
      })

    conn
    |> put_resp_content_type("application/json")
    |> put_resp_header("retry-after", "60")
    |> send_resp(:too_many_requests, body)
    |> halt()
  end
end
