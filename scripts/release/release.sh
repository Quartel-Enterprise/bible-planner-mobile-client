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

# --- release type ---------------------------------------------------------
echo "Release type:"
echo "  1) production   (default)"
echo "  2) pre-release  (beta — Play internal + TestFlight only, no desktop)"
case "$(ask 'choose [1]: ')" in
  ""|1) prerelease="false" ;;
  2)    prerelease="true" ;;
  *)    echo "Invalid option." >&2; exit 1 ;;
esac
echo

# --- version --------------------------------------------------------------
echo "Version name (X.Y.Z). Leave blank to auto-infer from commits."
if [ "$prerelease" = "true" ]; then
  echo "The beta number is appended automatically (e.g. 2.4.0-beta-1)."
fi
version="$(ask 'version [auto]: ')"
if [ -n "$version" ] && ! printf '%s' "$version" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  echo "Error: version must be in X.Y.Z format (e.g. 1.14.0)." >&2
  exit 1
fi
echo

if [ "$prerelease" = "true" ]; then
  platforms="mobile"
  track="internal"
  submit_ios="false"
else
  # --- platforms ----------------------------------------------------------
  echo "Platforms to release:"
  echo "  1) all      (default)"
  echo "  2) mobile   (android + ios)"
  echo "  3) android"
  echo "  4) ios"
  echo "  5) desktop"
  case "$(ask 'choose [1]: ')" in
    ""|1) platforms="all" ;;
    2)    platforms="mobile" ;;
    3)    platforms="android" ;;
    4)    platforms="ios" ;;
    5)    platforms="desktop" ;;
    *)    echo "Invalid option." >&2; exit 1 ;;
  esac
  echo

  # --- track --------------------------------------------------------------
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
fi

# --- complete_android_release --------------------------------------------
echo "Roll out the Android release? (No = upload to the track as a draft)"
case "$(ask 'roll out? [Y/n]: ')" in
  ""|[Yy]*) complete_android="true" ;;
  [Nn]*)    complete_android="false" ;;
  *)        echo "Invalid option." >&2; exit 1 ;;
esac
echo

if [ "$prerelease" != "true" ]; then
  # --- submit_ios_for_review ----------------------------------------------
  echo "Submit the iOS build for App Store review? (No = upload to TestFlight only)"
  case "$(ask 'submit? [Y/n]: ')" in
    ""|[Yy]*) submit_ios="true" ;;
    [Nn]*)    submit_ios="false" ;;
    *)        echo "Invalid option." >&2; exit 1 ;;
  esac
  echo
fi

# --- summary & confirm ----------------------------------------------------
echo "Review:"
echo "  release type             : $([ "$prerelease" = "true" ] && echo "pre-release (beta)" || echo "production")"
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

dispatch_release "$version" "$platforms" "$track" "$complete_android" "$submit_ios" "$prerelease"
