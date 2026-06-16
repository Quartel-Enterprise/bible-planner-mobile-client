#!/usr/bin/env bash
#
# reset-desktop-data.sh — wipe the desktop (JVM) app's local data for a clean launch.
#
# Clears the Room database, DataStore preferences, and the Supabase auth session, so the
# next launch starts fresh and logged out. Useful for testing migrations, onboarding, or
# logout data clearing. If the desktop app is running it offers to close it first, since
# deleting the DB under its lock corrupts the live instance.
#
# Usage: scripts/reset-desktop-data.sh
set -euo pipefail

# Run from the repository root regardless of the caller's working directory.
cd "$(dirname "$0")/.."

# Desktop app process marker — the JVM main class, present in the command line whether the
# app runs via Gradle (:desktopApp:run) or as a packaged distribution.
desktop_app_marker="com.quare.bibleplanner.MainKt"

# Room DB: DatabaseConstructor.desktop.kt stores it under $TMPDIR with -wal/-shm/.lck sidecars.
db_prefix="${TMPDIR:-/tmp}"
db_path="${db_prefix%/}/bible_planner.db"

# DataStore: CreateDataStore.jvm.kt writes prefs.preferences_pb relative to the launch dir.
datastore_files=(
  "desktopApp/prefs.preferences_pb"
  "prefs.preferences_pb"
)

deleted=0

# Clears the Supabase session, which supabase-kt persists via java.util.prefs.Preferences
# (not DataStore/Room) as a top-level `sb-<ref>-supabase-co-session` key. A leftover/expired
# token keeps the account logged in and breaks authenticated storage calls (e.g. version
# listing). Edits go through the Preferences API (not the plist, which cfprefsd would rewrite)
# and touch only `sb-*`/`supabase*` keys, leaving IDE child nodes (google/jetbrains/...) intact.
clear_supabase_session() {
  local java_bin=""
  for candidate in \
    "${JAVA_HOME:+$JAVA_HOME/bin/java}" \
    "$(command -v java 2>/dev/null || true)" \
    /opt/homebrew/opt/openjdk@21/bin/java \
    /opt/homebrew/opt/openjdk/bin/java \
    /usr/bin/java; do
    if [ -n "$candidate" ] && [ -x "$candidate" ]; then
      java_bin="$candidate"
      break
    fi
  done

  if [ -z "$java_bin" ]; then
    echo "WARNING: no Java runtime found — could not clear the Supabase auth session."
    echo "         The account will stay logged in. Install a JDK or set JAVA_HOME and re-run."
    return
  fi

  # Single-file source launcher is more portable across JDKs than jshell.
  # cwd is the repo root (see the cd above), so this path is stable.
  "$java_bin" scripts/support/ClearSupabaseSession.java
}

# Refuses to touch the data while the desktop app is running. Detects the live instance by
# its main class and, if found, asks the user whether to close it and continue or to cancel.
ensure_desktop_app_closed() {
  local pids
  pids="$(pgrep -f -- "$desktop_app_marker" 2>/dev/null || true)"
  if [ -z "$pids" ]; then
    return
  fi

  echo "The desktop app is running (PID: $(echo "$pids" | tr '\n' ' '))."
  echo "Deleting its data while it runs can corrupt the live instance."

  if [ ! -t 0 ]; then
    echo "Refusing to continue in a non-interactive shell. Close the app and re-run."
    exit 1
  fi

  echo "  1) Close the desktop app and continue"
  echo "  2) Cancel — do not run the script"
  local choice=""
  read -r -p "Choose [1/2]: " choice || true

  if [ "$choice" != "1" ]; then
    echo "Cancelled. No data was deleted."
    exit 0
  fi

  echo "Closing the desktop app..."
  pkill -f -- "$desktop_app_marker" 2>/dev/null || true

  # Wait up to 10s for a graceful shutdown, then force-kill any survivor.
  local waited=0
  while [ "$waited" -lt 10 ] && pgrep -f -- "$desktop_app_marker" >/dev/null 2>&1; do
    sleep 1
    waited=$((waited + 1))
  done
  if pgrep -f -- "$desktop_app_marker" >/dev/null 2>&1; then
    echo "App did not exit gracefully — forcing termination."
    pkill -9 -f -- "$desktop_app_marker" 2>/dev/null || true
    sleep 1
  fi
  echo "Desktop app closed."
}

ensure_desktop_app_closed

for file in "${db_path}"*; do
  if [ -e "$file" ]; then
    rm -f "$file"
    echo "Deleted $file"
    deleted=1
  fi
done

for file in "${datastore_files[@]}"; do
  if [ -e "$file" ]; then
    rm -f "$file"
    echo "Deleted $file"
    deleted=1
  fi
done

clear_supabase_session

if [ "$deleted" -eq 0 ]; then
  echo "No desktop local data found — auth session (if any) cleared above."
else
  echo "Desktop local data cleared. The next launch will start fresh and logged out."
fi
