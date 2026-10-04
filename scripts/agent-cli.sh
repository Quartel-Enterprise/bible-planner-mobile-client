#!/usr/bin/env bash
# Drives the app's real ViewModels headlessly, for coding agents. See docs/agent-cli.md.
#
# Usage:
#   scripts/agent-cli.sh start [--fresh] [--wide]   build, then start a warm session in the background
#   scripts/agent-cli.sh <command...>               send one command, e.g. `state DayViewModel.uiState`
#   scripts/agent-cli.sh -                          send every line of stdin as a command
#   scripts/agent-cli.sh stop                       stop the session
#   scripts/agent-cli.sh repl [--fresh] [--wide]    an interactive session on stdin instead
#
# Each worktree gets its own session, data and port under tools/agent-cli/build/agent-cli-session.
# AGENT_CLI_DATA_DIR points the session at another data directory.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
build="$root/tools/agent-cli/build"
session="$build/agent-cli-session"
launcher="$build/agent-cli-launcher"
data="${AGENT_CLI_DATA_DIR:-$session/data}"
[[ "$data" = /* ]] || data="$PWD/$data"
port_file="$session/port"
pid_file="$session/pid"
log_file="$session/agent-cli.log"
main_class="com.quare.bibleplanner.tools.agentcli.MainKt"

build_launcher() {
  "$root/gradlew" -p "$root" -q :tools:agent-cli:agentCliLauncher
}

# The app prefers XDG_DATA_HOME (Linux) and APPDATA (Windows) over user.home for its database, so
# both are pointed inside the data directory too.
java_command() {
  java_args=(env "XDG_DATA_HOME=$data/home/.local/share" "APPDATA=$data/home/AppData/Roaming"
    "$(cat "$launcher/java")" -Djava.awt.headless=true -cp "$(cat "$launcher/classpath")" "$main_class"
    --data-dir "$data")
}

is_running() {
  [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null
}

# Waits for the old process to exit, so a new session never opens a database it still has open.
stop_session() {
  if is_running; then
    pid="$(cat "$pid_file")"
    kill "$pid"
    for _ in $(seq 1 50); do
      kill -0 "$pid" 2>/dev/null || break
      sleep 0.2
    done
    kill -9 "$pid" 2>/dev/null || true
  fi
  rm -f "$pid_file" "$port_file"
}

send() {
  if ! is_running || [[ ! -f "$port_file" ]]; then
    echo "agent-cli is not running; start it with scripts/agent-cli.sh start" >&2
    exit 1
  fi
  curl --silent --show-error --fail-with-body -X POST --data-binary @- "http://127.0.0.1:$(cat "$port_file")/"
}

case "${1:-}" in
  start)
    shift
    stop_session
    build_launcher
    mkdir -p "$session"
    java_command
    nohup "${java_args[@]}" --serve --port-file "$port_file" "$@" > "$log_file" 2>&1 &
    echo $! > "$pid_file"
    disown
    for _ in $(seq 1 240); do
      if [[ -f "$port_file" ]]; then
        echo "agent-cli ready on port $(cat "$port_file") (log: $log_file)"
        exit 0
      fi
      if ! is_running; then
        tail -n 40 "$log_file" >&2
        echo "agent-cli failed to start" >&2
        exit 1
      fi
      sleep 0.5
    done
    echo "agent-cli did not start within 2 minutes; see $log_file" >&2
    exit 1
    ;;
  stop)
    stop_session
    ;;
  repl)
    shift
    build_launcher
    java_command
    exec "${java_args[@]}" "$@"
    ;;
  -)
    send
    ;;
  "" | -h | --help)
    sed -n '2,13p' "$0" | sed 's/^# \{0,1\}//'
    ;;
  *)
    printf '%s' "$*" | send
    ;;
esac
