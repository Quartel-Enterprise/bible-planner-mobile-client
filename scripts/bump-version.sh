#!/usr/bin/env bash
# Bumps the app version across all platforms.
#
# Usage: scripts/bump-version.sh <versionName> <versionCode>
#   versionName  e.g. 1.14.0  — Android versionName / iOS MARKETING_VERSION / Desktop packageVersion
#   versionCode  e.g. 23      — Android versionCode  / iOS CURRENT_PROJECT_VERSION
#
# Touches exactly three files (Android, iOS, Desktop), matching the
# established release bump commit.
set -euo pipefail

version="${1:?versionName required (e.g. 1.14.0)}"
code="${2:?versionCode required (e.g. 23)}"

# Run from the repository root regardless of the caller's working directory.
cd "$(dirname "$0")/.."

# Android — gradle.properties
sed -i.bak -E "s/^versionCode=.*/versionCode=${code}/" gradle.properties
sed -i.bak -E "s/^versionName=.*/versionName=${version}/" gradle.properties

# iOS — Configuration/Config.xcconfig
sed -i.bak -E "s/^CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=${code}/" iosApp/Configuration/Config.xcconfig
sed -i.bak -E "s/^MARKETING_VERSION=.*/MARKETING_VERSION=${version}/" iosApp/Configuration/Config.xcconfig

# Desktop — desktopApp/build.gradle.kts
sed -i.bak -E "s/packageVersion = \"[^\"]*\"/packageVersion = \"${version}\"/" desktopApp/build.gradle.kts

rm -f gradle.properties.bak \
      iosApp/Configuration/Config.xcconfig.bak \
      desktopApp/build.gradle.kts.bak

echo "Bumped version to ${version} (versionCode ${code})"
