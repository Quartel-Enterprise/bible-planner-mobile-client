#!/usr/bin/env bash
# Uploads iOS dSYMs to Firebase Crashlytics by hand.
#
# Use this for one-off "Missing dSYM" alerts — e.g. a build that shipped before
# the Fastlane upload step existed, or one whose dSYMs Apple regenerated. The
# release pipeline already uploads dSYMs automatically (fastlane ios build), so
# you should rarely need this.
#
# Usage: scripts/upload_dsyms.sh [-n|--dry-run] <path-to-dSYMs>
#   <path-to-dSYMs>  a .dSYM bundle, a folder of them, or a .zip
#                    (e.g. the archive downloaded from App Store Connect / Xcode
#                    Organizer → Download Debug Symbols)
#   -n, --dry-run    validate the binary, plist and dSYMs without uploading
#                    (passes upload-symbols' --validate flag)
#
# The Crashlytics upload-symbols binary is resolved from SPM checkouts: first
# the pinned CI path (build/derived_data), then the default Xcode DerivedData.
set -euo pipefail

dry_run=""
case "${1:-}" in
  -n|--dry-run) dry_run="-val"; shift ;;
esac

dsym_path="${1:?path to dSYMs required (a .dSYM, a folder, or a .zip)}"

# Run from the repository root regardless of the caller's working directory.
cd "$(dirname "$0")/.."

if [ ! -e "$dsym_path" ]; then
  echo "error: '$dsym_path' does not exist" >&2
  exit 1
fi

gsp_path="iosApp/iosApp/GoogleService-Info.plist"
if [ ! -f "$gsp_path" ]; then
  echo "error: $gsp_path not found — decode it before running this script" >&2
  exit 1
fi

# Locate upload-symbols: the pinned CI derived-data path first, then a glob over
# the default DerivedData (most recently modified wins).
upload_symbols="build/derived_data/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols"
if [ ! -x "$upload_symbols" ]; then
  upload_symbols="$(find "$HOME/Library/Developer/Xcode/DerivedData" \
    -path '*SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols' \
    2>/dev/null | head -1)"
fi

if [ -z "${upload_symbols:-}" ] || [ ! -x "$upload_symbols" ]; then
  echo "error: Crashlytics upload-symbols binary not found." >&2
  echo "       Build the iOS app once in Xcode to resolve the Firebase SPM package, then retry." >&2
  exit 1
fi

if [ -n "$dry_run" ]; then
  echo "Validating dSYMs from '$dsym_path' (no upload)..."
  "$upload_symbols" -gsp "$gsp_path" -p ios "$dry_run" -- "$dsym_path"
  echo "Validation passed — setup is correct."
else
  echo "Uploading dSYMs from '$dsym_path' to Crashlytics..."
  "$upload_symbols" -gsp "$gsp_path" -p ios "$dsym_path"
  echo "Done. The Missing dSYM alert clears in a few minutes."
fi
