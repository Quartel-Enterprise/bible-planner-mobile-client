#!/usr/bin/env bash
# Resolves the version for a release run.
#
# Usage: scripts/suggest-version.sh [versionName]
#   versionName given  -> used as-is.
#   versionName omitted -> inferred from Conventional Commit prefixes since
#   the last release: any feature/feat commit -> minor bump, otherwise patch.
#
# versionCode is always the current value + 1.
#
# Inside GitHub Actions, writes `version`, `version_code` and `branch` to
# $GITHUB_OUTPUT and a human-readable plan to $GITHUB_STEP_SUMMARY.
set -euo pipefail

cd "$(dirname "$0")/.."

input_version="${1:-}"

if [[ -n "$input_version" && ! "$input_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Error: version '$input_version' must be in X.Y.Z format (e.g. 1.14.0)" >&2
  exit 1
fi

current_version="$(grep -E '^versionName=' gradle.properties | cut -d= -f2)"
current_code="$(grep -E '^versionCode=' gradle.properties | cut -d= -f2)"
next_code=$(( current_code + 1 ))

if [[ -n "$input_version" ]]; then
  version="$input_version"
  source_desc="provided manually via the workflow input"
else
  # Anchor = last v* tag, falling back to the last "merge back" commit.
  anchor="$(git describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)"
  if [[ -z "$anchor" ]]; then
    anchor="$(git log --grep='merge back' --format='%H' -n 1 2>/dev/null || true)"
  fi
  range="HEAD"
  [[ -n "$anchor" ]] && range="${anchor}..HEAD"

  if git log "$range" --format='%s' | grep -qiE '^(feat|feature)(\(.+\))?!?:'; then
    bump="minor"
  else
    bump="patch"
  fi

  IFS='.' read -r major minor patch <<< "$current_version"
  if [[ "$bump" == "minor" ]]; then
    minor=$(( minor + 1 )); patch=0
  else
    patch=$(( patch + 1 ))
  fi
  version="${major}.${minor}.${patch}"
  source_desc="inferred (${bump} bump from commits since ${anchor:-the start of history})"
fi

branch="release/${version}"

echo "Resolved version: ${version} (versionCode ${next_code}) — ${source_desc}"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "version=${version}"
    echo "version_code=${next_code}"
    echo "branch=${branch}"
  } >> "$GITHUB_OUTPUT"
fi

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "## Release plan"
    echo ""
    echo "| Field | Value |"
    echo "|---|---|"
    echo "| Version name | \`${version}\` |"
    echo "| Version code | \`${next_code}\` |"
    echo "| Release branch | \`${branch}\` |"
    echo "| Source | ${source_desc} |"
    echo ""
    echo "Review this plan, then **approve the \`Production\` environment** to continue."
    echo "To use a different version, reject this run and start a new one with the \`version\` input set."
  } >> "$GITHUB_STEP_SUMMARY"
fi
