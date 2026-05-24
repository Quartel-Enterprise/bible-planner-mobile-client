#!/usr/bin/env bash
#
# Trigger the Bible Planner release workflow from the terminal — no GitHub UI.
#
# Shows a menu for each workflow input (defaults match the GitHub UI),
# dispatches the `release` workflow and prints the link to the new run.
#
# Requires the GitHub CLI (gh), installed and authenticated:
#   https://cli.github.com
#
# Usage: ./scripts/release/release.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./_common.sh
source "$SCRIPT_DIR/_common.sh"

# Prints a prompt to stderr and echoes the typed answer on stdout.
# Works in both bash and zsh (avoids the shell-specific `read -p`).
ask() {
  local answer
  printf '%s' "$1" >&2
  read -r answer
  printf '%s' "$answer"
}

echo "Bible Planner — trigger a release"
echo "================================="
echo

# --- version --------------------------------------------------------------
echo "Version name (X.Y.Z). Leave blank to auto-infer from commits."
version="$(ask 'version [auto]: ')"
if [ -n "$version" ] && ! printf '%s' "$version" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  echo "Error: version must be in X.Y.Z format (e.g. 1.14.0)." >&2
  exit 1
fi
echo

# --- platforms ------------------------------------------------------------
echo "Platforms to release:"
echo "  1) both     (default)"
echo "  2) android"
echo "  3) ios"
case "$(ask 'choose [1]: ')" in
  ""|1) platforms="both" ;;
  2)    platforms="android" ;;
  3)    platforms="ios" ;;
  *)    echo "Invalid option." >&2; exit 1 ;;
esac
echo

# --- track ----------------------------------------------------------------
echo "Android Play Store track:"
echo "  1) production  (default)"
echo "  2) beta"
echo "  3) alpha"
echo "  4) internal"
case "$(ask 'choose [1]: ')" in
  ""|1) track="production" ;;
  2)    track="beta" ;;
  3)    track="alpha" ;;
  4)    track="internal" ;;
  *)    echo "Invalid option." >&2; exit 1 ;;
esac
echo

# --- complete_android_release --------------------------------------------
echo "Roll out the Android release? (No = upload to the track as a draft)"
case "$(ask 'roll out? [Y/n]: ')" in
  ""|[Yy]*) complete_android="true" ;;
  [Nn]*)    complete_android="false" ;;
  *)        echo "Invalid option." >&2; exit 1 ;;
esac
echo

# --- submit_ios_for_review ------------------------------------------------
echo "Submit the iOS build for App Store review? (No = upload to TestFlight only)"
case "$(ask 'submit? [Y/n]: ')" in
  ""|[Yy]*) submit_ios="true" ;;
  [Nn]*)    submit_ios="false" ;;
  *)        echo "Invalid option." >&2; exit 1 ;;
esac
echo

# --- summary & confirm ----------------------------------------------------
echo "Review:"
echo "  version                  : ${version:-(auto-infer)}"
echo "  platforms                : $platforms"
echo "  track                    : $track"
echo "  complete_android_release : $complete_android"
echo "  submit_ios_for_review    : $submit_ios"
echo
case "$(ask 'Trigger this release? [y/N]: ')" in
  [Yy]*) ;;
  *) echo "Cancelled."; exit 0 ;;
esac

dispatch_release "$version" "$platforms" "$track" "$complete_android" "$submit_ios"
