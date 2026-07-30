#!/usr/bin/env bash
# Bumps the app version across all platforms.
#
# Usage: scripts/bump-version.sh <versionName> <versionCode>
#   versionName  e.g. 1.14.0  — Android versionName / iOS MARKETING_VERSION
#   versionCode  e.g. 23      — Android versionCode  / iOS CURRENT_PROJECT_VERSION
#
# Touches a single file: version.xcconfig at the repository root. Android and desktop
# read it through the root Gradle script, and iOS through Config.xcconfig's `#include`.
set -euo pipefail

version="${1:?versionName required (e.g. 1.14.0)}"
code="${2:?versionCode required (e.g. 23)}"

# Run from the repository root regardless of the caller's working directory.
cd "$(dirname "$0")/.."

sed -i.bak -E "s/^versionName=.*/versionName=${version}/" version.xcconfig
sed -i.bak -E "s/^versionCode=.*/versionCode=${code}/" version.xcconfig

rm -f version.xcconfig.bak

echo "Bumped version to ${version} (versionCode ${code})"
