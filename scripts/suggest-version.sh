#!/usr/bin/env bash
# Resolves the version for a release run.
#
# Usage: scripts/suggest-version.sh [versionName]
#
# Resolution order:
#   1. The versionName argument, if provided.
#   2. The highest version key in the in-app release notes JSON, when it is
#      ahead of the currently released version — this is the team's declared
#      next version and also the source of the App Store "What's New".
#   3. Otherwise inferred from Conventional Commit prefixes since the last
#      release: any feature/feat commit -> minor bump, otherwise patch.
#
# versionCode is always the current value + 1.
#
# Inside GitHub Actions, writes `version`, `version_code` and `branch` to
# $GITHUB_OUTPUT and a human-readable plan to $GITHUB_STEP_SUMMARY.
set -euo pipefail

cd "$(dirname "$0")/.."

EN_JSON="feature/release_notes/src/commonMain/composeResources/files/release_notes/en.json"

input_version="${1:-}"

if [[ -n "$input_version" && ! "$input_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Error: version '$input_version' must be in X.Y.Z format (e.g. 1.14.0)" >&2
  exit 1
fi

current_version="$(grep -E '^versionName=' gradle.properties | cut -d= -f2)"
current_code="$(grep -E '^versionCode=' gradle.properties | cut -d= -f2)"
next_code=$(( current_code + 1 ))

# Highest X.Y.Z key declared in the release notes JSON ("" if none/unavailable).
json_version="$(python3 -c "import json; ks=[k for k in json.load(open('$EN_JSON')) if k.count('.')==2 and k.replace('.','').isdigit()]; print(max(ks, key=lambda v: tuple(int(x) for x in v.split('.'))) if ks else '')" 2>/dev/null || true)"

# is_higher A B -> succeeds when semver A is strictly greater than B.
is_higher() {
  [[ "$1" != "$2" && "$(printf '%s\n%s\n' "$1" "$2" | sort -V | tail -1)" == "$1" ]]
}

if [[ -n "$input_version" ]]; then
  version="$input_version"
  source_desc="provided manually via the workflow input"
elif [[ -n "$json_version" ]] && is_higher "$json_version" "$current_version"; then
  version="$json_version"
  source_desc="taken from the release notes JSON (the team's declared next version)"
else
  # Anchor = last X.Y.Z tag, falling back to the last "merge back" commit.
  anchor="$(git describe --tags --abbrev=0 --match '[0-9]*' 2>/dev/null || true)"
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
  source_desc="inferred (${bump} bump from commits — the release notes JSON has no newer version yet)"
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
