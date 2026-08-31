#!/usr/bin/env bash
#
# Trigger the Bible Planner release workflow for a mobile-only production
# release (android + ios) — no prompts, no menu.
#
# Version is auto-inferred from commits. Android rolls out to production
# and iOS is submitted for App Store review. Desktop is skipped.
#
# Requires the GitHub CLI (gh), installed and authenticated:
#   https://cli.github.com
#
# Usage: ./scripts/release/prod/release-prod-mobile.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../_common.sh
source "$SCRIPT_DIR/../_common.sh"

echo "Bible Planner — production release (android + ios)"
echo "=================================================="

dispatch_release "" "mobile" "production" "true" "true" "false"
