#!/usr/bin/env bash
# Deletes the desktop (JVM) app's local data so the next launch starts from a clean
# state: a fresh Room database and no DataStore preferences. Useful for testing
# database migrations, the first-launch/onboarding flow, or logout data clearing.
#
# Close the desktop app before running — deleting the database while the running
# instance holds its lock can leave that instance in a bad state.
#
# Usage: scripts/reset-desktop-data.sh
set -euo pipefail

# Run from the repository root regardless of the caller's working directory.
cd "$(dirname "$0")/.."

# Room database — DatabaseConstructor.desktop.kt stores it under java.io.tmpdir
# (== $TMPDIR on macOS/Linux) using DatabaseUtils.PATH, alongside the SQLite
# -wal/-shm/.lck sidecar files.
db_prefix="${TMPDIR:-/tmp}"
db_path="${db_prefix%/}/bible_planner.db"

# DataStore — CreateDataStore.jvm.kt writes prefs.preferences_pb relative to the
# app's working directory, which is the composeApp module when launched via Gradle.
datastore_files=(
  "composeApp/prefs.preferences_pb"
  "prefs.preferences_pb"
)

deleted=0

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

if [ "$deleted" -eq 0 ]; then
  echo "No desktop local data found — nothing to delete."
else
  echo "Desktop local data cleared. The next launch will start fresh."
fi
