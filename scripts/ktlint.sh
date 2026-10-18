#!/bin/bash

# Runs ktlint with the Quare ruleset (github.com/Quartel-Enterprise/ktlint-rules):
#   ./scripts/ktlint.sh            check
#   ./scripts/ktlint.sh --format   autocorrect what can be autocorrected
#
# This file never changes: the ruleset version pinned in gradle/libs.versions.toml (quare-ktlint-rules) or in
# .ktlint-rules-version picks the runner, the rules and the ktlint CLI, all downloaded from that release.

set -e

ROOT="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." > /dev/null && pwd)"

if [ -f "$ROOT/.ktlint-rules-version" ]; then
    VERSION="$(tr -d '[:space:]' < "$ROOT/.ktlint-rules-version")"
else
    VERSION="$(sed -nE 's/^quare-ktlint-rules[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' \
        "$ROOT/gradle/libs.versions.toml" 2> /dev/null | head -1)"
fi

if [ -z "$VERSION" ]; then
    echo "❌ Pin the ruleset version: quare-ktlint-rules in gradle/libs.versions.toml, or .ktlint-rules-version"
    exit 1
fi

CACHE="${KTLINT_CACHE_DIR:-$HOME/.cache/ktlint}"
RUNNER="$CACHE/runner-$VERSION.sh"

if [ ! -f "$RUNNER" ]; then
    mkdir -p "$CACHE"
    TMP="$(mktemp "$RUNNER.XXXXXX")"
    if ! curl -fsSLo "$TMP" \
        "https://raw.githubusercontent.com/Quartel-Enterprise/ktlint-rules/$VERSION/scripts/ktlint.sh"; then
        rm -f "$TMP"
        echo "❌ Failed to download the ktlint runner of the ruleset $VERSION"
        exit 1
    fi
    mv "$TMP" "$RUNNER"
fi

KTLINT_PROJECT_ROOT="$ROOT" exec bash "$RUNNER" "$VERSION" "$@"
