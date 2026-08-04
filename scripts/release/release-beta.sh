#!/usr/bin/env bash
#
# Trigger the Bible Planner release workflow for a pre-release (beta)
# on mobile — no prompts, no menu.
#
# Version is auto-inferred from commits and named X.Y.Z-beta-N (the beta
# number is computed from the existing beta tags). Android rolls out to the
# Play internal track, iOS goes to TestFlight without App Store review,
# desktop is skipped, and the GitHub release is marked as a pre-release.
#
# Requires the GitHub CLI (gh), installed and authenticated:
#   https://cli.github.com
#
# Usage: ./scripts/release/release-beta.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./_common.sh
source "$SCRIPT_DIR/_common.sh"

echo "Bible Planner — pre-release beta (android internal + ios testflight)"
echo "===================================================================="

dispatch_release "" "mobile" "internal" "true" "false" "true"
