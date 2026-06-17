#!/usr/bin/env bash
#
# install-latest-release-android.sh — build the latest published release and install it on
# the connected Android emulator/device, for testing in-place upgrades and DB migrations.
#
# Steps:
#   1. If the app is already installed, asks before uninstalling it (uninstall wipes its data).
#   2. Resolves the latest GitHub release tag (stripping a leading 'v', e.g. v1.14.0 -> 1.14.0).
#   3. Checks that tag out into a throwaway git worktree (your working tree is left untouched).
#   4. Builds the debug APK from that tag and installs it.
#
# Then build & install your current branch on top to watch the migration run against the
# data the old version produced. Both this and a local debug build are debug-signed, so the
# upgrade installs in place. Releases ship no APK asset, so the tag is built from source.
#
# Usage: scripts/install-latest-release-android.sh
set -euo pipefail

# Run from the repository root regardless of the caller's working directory.
cd "$(dirname "$0")/.."

app_id="com.quare.bibleplanner"

# Resolve adb: prefer $ANDROID_HOME/$ANDROID_SDK_ROOT, then the default macOS SDK location,
# then PATH. The bare `adb` is often not on PATH even when the SDK is installed.
resolve_adb() {
  for candidate in \
    "${ANDROID_HOME:+$ANDROID_HOME/platform-tools/adb}" \
    "${ANDROID_SDK_ROOT:+$ANDROID_SDK_ROOT/platform-tools/adb}" \
    "$HOME/Library/Android/sdk/platform-tools/adb" \
    "$(command -v adb 2>/dev/null || true)"; do
    if [ -n "$candidate" ] && [ -x "$candidate" ]; then
      echo "$candidate"
      return
    fi
  done
  echo "ERROR: adb not found. Set ANDROID_HOME or add platform-tools to PATH." >&2
  exit 1
}

adb="$(resolve_adb)"

# Picks the target device. Honors $ANDROID_SERIAL if set; otherwise requires exactly one
# connected device so we never install onto the wrong target by accident.
resolve_serial() {
  if [ -n "${ANDROID_SERIAL:-}" ]; then
    echo "$ANDROID_SERIAL"
    return
  fi
  local devices
  devices="$("$adb" devices | awk 'NR>1 && $2=="device" {print $1}')"
  local count
  count="$(printf '%s\n' "$devices" | grep -c . || true)"
  if [ "$count" -eq 0 ]; then
    echo "ERROR: no connected device. Start an emulator or plug in a device." >&2
    exit 1
  fi
  if [ "$count" -gt 1 ]; then
    echo "ERROR: multiple devices connected. Set ANDROID_SERIAL to choose one:" >&2
    printf '%s\n' "$devices" >&2
    exit 1
  fi
  echo "$devices"
}

serial="$(resolve_serial)"
echo "Target device: $serial"

# Latest release tag straight from GitHub, with any leading 'v' stripped for the build ref.
if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: gh CLI not found. Install it and run 'gh auth login'." >&2
  exit 1
fi
release_tag="$(gh release view --json tagName -q .tagName)"
version="${release_tag#v}"
echo "Latest release: $release_tag (building '$version')"

# Make sure the tag is available locally before checking it out.
git fetch --tags --quiet

# Uninstall whatever is currently installed, so the old release installs against empty data.
# Only ask (and only uninstall) if the app is actually present — otherwise there's nothing to do.
if "$adb" -s "$serial" shell pm list packages | grep -q "^package:${app_id}$"; then
  echo "$app_id is installed on $serial. Uninstalling it will wipe its local data."
  if [ ! -t 0 ]; then
    echo "Refusing to uninstall in a non-interactive shell. Re-run from a terminal." >&2
    exit 1
  fi
  read -r -p "Uninstall it and continue? [y/N]: " answer || true
  case "$answer" in
    [yY] | [yY][eE][sS]) ;;
    *)
      echo "Cancelled. Nothing was uninstalled."
      exit 0
      ;;
  esac
  echo "Uninstalling current $app_id ..."
  "$adb" -s "$serial" uninstall "$app_id" >/dev/null
  echo "Uninstalled."
else
  echo "$app_id is not installed — nothing to uninstall."
fi

# Build the tag in a throwaway worktree so the working tree and current branch are untouched.
build_root="$(mktemp -d)"
worktree_dir="$build_root/worktree"
cleanup() {
  git worktree remove --force "$worktree_dir" >/dev/null 2>&1 || true
  rm -rf "$build_root"
}
trap cleanup EXIT

echo "Checking out '$version' into a temporary worktree ..."
git worktree add --detach "$worktree_dir" "$version" >/dev/null

# These files are gitignored, so the fresh worktree lacks them: local.properties holds the
# SDK location and the Supabase/RevenueCat/GH secrets, and google-services.json is required
# by the Google Services plugin. Copy this checkout's copies over so the build resolves the
# SDK and the installed app can actually reach Supabase to download Bible content.
required_local_files=(
  "local.properties"
  "androidApp/google-services.json"
)
for file in "${required_local_files[@]}"; do
  if [ ! -f "$file" ]; then
    echo "ERROR: $file not found in this checkout — the worktree build needs it." >&2
    exit 1
  fi
  mkdir -p "$worktree_dir/$(dirname "$file")"
  cp "$file" "$worktree_dir/$file"
done

echo "Building debug APK from '$version' (this can take a few minutes) ..."
(cd "$worktree_dir" && ./gradlew :androidApp:assembleDebug --console=plain)

apk="$worktree_dir/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
if [ ! -f "$apk" ]; then
  echo "ERROR: expected APK not found at $apk" >&2
  exit 1
fi

echo "Installing $version onto $serial ..."
"$adb" -s "$serial" install "$apk"

echo
echo "Done. Version $version is installed on $serial."
