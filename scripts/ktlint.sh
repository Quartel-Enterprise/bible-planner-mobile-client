#!/bin/bash

# Runs ktlint with the Quare ruleset (github.com/Quartel-Enterprise/ktlint-rules):
#   ./scripts/ktlint.sh            check
#   ./scripts/ktlint.sh --format   autocorrect what can be autocorrected
#
# This file never changes: the ruleset version pinned in gradle/libs.versions.toml (quare-ktlint-rules), or in
# .ktlint-rules-version without a catalog, picks the runner, the rules and the ktlint CLI of that release.
# The runner comes from the release tag on GitHub (trusted like a GitHub Action pinned to a tag); the jar
# it runs is checked against the release signature.

set -e

ROOT="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." > /dev/null && pwd)"

VERSION="$(sed -nE 's/^quare-ktlint-rules[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' \
    "$ROOT/gradle/libs.versions.toml" 2> /dev/null | head -1)"

# Only a project without a version catalog uses the file
if [ -z "$VERSION" ] && [ -f "$ROOT/.ktlint-rules-version" ]; then
    VERSION="$(tr -d '[:space:]' < "$ROOT/.ktlint-rules-version")"
fi

# A release version, never a branch: its runner is cached for good, and release tags never move
if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "❌ Pin a ruleset release (X.Y.Z) as quare-ktlint-rules in gradle/libs.versions.toml, or in"
    echo "   .ktlint-rules-version for a project without a catalog. Found: '$VERSION'"
    exit 1
fi

CACHE="${KTLINT_CACHE_DIR:-$HOME/.cache/ktlint}"
RUNNER="$CACHE/runner-$VERSION.sh"

if [ ! -f "$RUNNER" ]; then
    mkdir -p "$CACHE"
    TMP="$(mktemp "$RUNNER.XXXXXX")"
    if ! curl -fsSLo "$TMP" \
        "https://raw.githubusercontent.com/Quartel-Enterprise/ktlint-rules/refs/tags/$VERSION/scripts/ktlint.sh"; then
        rm -f "$TMP"
        echo "❌ Failed to download the ktlint runner of the ruleset $VERSION"
        exit 1
    fi
    mv "$TMP" "$RUNNER"
fi

KTLINT_PROJECT_ROOT="$ROOT" exec bash "$RUNNER" "$VERSION" "$@"
