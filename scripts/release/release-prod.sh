#!/usr/bin/env bash
#
# Trigger the Bible Planner release workflow for a full production release
# on every platform — no prompts, no menu.
#
# Version is auto-inferred from commits. Android rolls out to production,
# iOS is submitted for App Store review, and the desktop installers are
# attached to the GitHub release.
#
# Requires the GitHub CLI (gh), installed and authenticated:
#   https://cli.github.com
#
# Usage: ./scripts/release/release-prod.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./_common.sh
source "$SCRIPT_DIR/_common.sh"

echo "Bible Planner — production release (android + ios + desktop)"
echo "============================================================"

dispatch_release "" "all" "production" "true" "true" "false"
