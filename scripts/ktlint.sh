#!/bin/bash

# Runs ktlint over the whole repository, with the project's custom ruleset.
# This is the same check the static-analysis workflow runs in CI.

set -e

# --format rewrites the offending files instead of only reporting them
FORMAT_MODE=false

if [ "$1" = "--format" ]; then
    FORMAT_MODE=true
fi

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Find project root by looking for gradlew or build.gradle.kts
# Start from script directory and go up until we find it
CURRENT_DIR="$SCRIPT_DIR"
PROJECT_ROOT=""

while [ "$CURRENT_DIR" != "/" ]; do
    if [ -f "$CURRENT_DIR/gradlew" ] || [ -f "$CURRENT_DIR/build.gradle.kts" ]; then
        PROJECT_ROOT="$CURRENT_DIR"
        break
    fi
    CURRENT_DIR="$(dirname "$CURRENT_DIR")"
done

if [ -z "$PROJECT_ROOT" ]; then
    echo "❌ Could not find project root (looking for gradlew or build.gradle.kts)"
    exit 1
fi

# Change to project root
cd "$PROJECT_ROOT"

echo "🔍 Running ktlint..."
echo "📁 Working directory: $PROJECT_ROOT"
echo ""

# Check if Java is available (the ktlint CLI is a self-executing jar)
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install JDK 21 (Temurin distribution recommended)."
    exit 1
fi

# The version catalog is the single source of truth for the ktlint version
KTLINT_VERSION=$(sed -nE 's/^ktlint[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' gradle/libs.versions.toml | head -1)

if [ -z "$KTLINT_VERSION" ]; then
    echo "❌ Could not read the ktlint version from gradle/libs.versions.toml"
    exit 1
fi

# The custom rules come from Maven Central (github.com/Quartel-Enterprise/ktlint-rules), pinned in the catalog too
RULESET_VERSION=$(sed -nE 's/^quare-ktlint-rules[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' gradle/libs.versions.toml | head -1)

if [ -z "$RULESET_VERSION" ]; then
    echo "❌ Could not read the quare-ktlint-rules version from gradle/libs.versions.toml"
    exit 1
fi

KTLINT_CACHE_DIR="${KTLINT_CACHE_DIR:-$HOME/.cache/ktlint}"
KTLINT_BIN="$KTLINT_CACHE_DIR/ktlint-$KTLINT_VERSION"
RULESET_JAR="$KTLINT_CACHE_DIR/ktlint-rules-$RULESET_VERSION.jar"
RULESET_URL="https://repo1.maven.org/maven2/software/quare/ktlint-rules/$RULESET_VERSION/ktlint-rules-$RULESET_VERSION.jar"
REPORT_PATH="build/ktlint-report.xml"

# Downloads $2 to $1 once, through a temporary file so an interrupted download never gets cached as valid
download_once() {
    local target="$1"
    local url="$2"
    local label="$3"

    if [ -f "$target" ]; then
        return
    fi

    echo "⬇️  Downloading $label..."
    mkdir -p "$KTLINT_CACHE_DIR"

    if ! curl -fsSLo "$target.tmp" "$url"; then
        rm -f "$target.tmp"
        echo "❌ Failed to download $label from $url"
        exit 1
    fi

    mv "$target.tmp" "$target"
    echo ""
}

download_once "$KTLINT_BIN" \
    "https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint" \
    "the ktlint $KTLINT_VERSION CLI"
chmod +x "$KTLINT_BIN"

# KTLINT_RULESET_JAR points at a local build of the ruleset, to try a rule change before releasing it
if [ -n "$KTLINT_RULESET_JAR" ]; then
    RULESET_JAR="$KTLINT_RULESET_JAR"
else
    download_once "$RULESET_JAR" "$RULESET_URL" "the custom ruleset $RULESET_VERSION"
fi

KTLINT_ARGS=(
    --ruleset="$RULESET_JAR"
    --relative
    --reporter=plain
)

if [ "$FORMAT_MODE" = true ]; then
    echo "🛠️  Running ktlint format..."
    KTLINT_ARGS+=(--format)
else
    echo "📋 Running ktlint check..."
    mkdir -p build
    KTLINT_ARGS+=(--reporter=checkstyle,output="$REPORT_PATH")
fi
echo ""

set +e
"$KTLINT_BIN" "${KTLINT_ARGS[@]}" \
    "**/src/**/*.kt" \
    "**/*.kts" \
    "!**/build/**" \
    "!**/generated/**"
KTLINT_EXIT_CODE=$?
set -e

echo ""
if [ $KTLINT_EXIT_CODE -ne 0 ]; then
    if [ "$FORMAT_MODE" = true ]; then
        echo "❌ ktlint found errors that cannot be autocorrected. Fix them by hand."
    else
        echo "❌ ktlint found formatting errors."
        echo ""
        echo "To fix them, run:"
        echo "  ./scripts/ktlint.sh --format"
    fi
    echo ""
    echo "✨ ktlint finished (with errors)!"
    exit $KTLINT_EXIT_CODE
fi

if [ "$FORMAT_MODE" = false ]; then
    echo "✅ Report saved to: $REPORT_PATH"
fi
echo "✨ ktlint finished!"
