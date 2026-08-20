#!/bin/bash

# Script to simulate CI locally
# This runs the same checks that GitHub Actions runs

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

echo "🔍 Simulating CI locally..."
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

KTLINT_CACHE_DIR="${KTLINT_CACHE_DIR:-$HOME/.cache/ktlint}"
KTLINT_BIN="$KTLINT_CACHE_DIR/ktlint-$KTLINT_VERSION"
RULESET_JAR="tools/ktlint-custom-rules/build/libs/ktlint-custom-rules.jar"
REPORT_PATH="build/ktlint-report.xml"

# Download the ktlint CLI once per version and reuse it afterwards
if [ ! -x "$KTLINT_BIN" ]; then
    echo "⬇️  Downloading ktlint $KTLINT_VERSION CLI..."
    mkdir -p "$KTLINT_CACHE_DIR"

    # Download to a temporary file first so an interrupted download never gets cached as valid
    if ! curl -fsSLo "$KTLINT_BIN.tmp" \
        "https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint"; then
        rm -f "$KTLINT_BIN.tmp"
        echo "❌ Failed to download the ktlint $KTLINT_VERSION CLI"
        exit 1
    fi

    chmod +x "$KTLINT_BIN.tmp"
    mv "$KTLINT_BIN.tmp" "$KTLINT_BIN"
    echo ""
fi

# Rebuild the custom ruleset jar whenever it is missing or older than its sources
NEEDS_RULESET_BUILD=false

if [ ! -f "$RULESET_JAR" ]; then
    NEEDS_RULESET_BUILD=true
elif [ -n "$(find tools/ktlint-custom-rules/src tools/ktlint-custom-rules/build.gradle.kts \
        -newer "$RULESET_JAR" -print -quit 2> /dev/null)" ]; then
    NEEDS_RULESET_BUILD=true
fi

if [ "$NEEDS_RULESET_BUILD" = true ]; then
    if [ ! -f "./gradlew" ]; then
        echo "❌ gradlew not found in project root: $PROJECT_ROOT"
        exit 1
    fi

    echo "🔨 Building the custom ktlint ruleset..."
    chmod +x ./gradlew
    ./gradlew :tools:ktlint-custom-rules:jar --quiet
    echo ""
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
        echo "  ./scripts/ci-local.sh --format"
    fi
    echo ""
    echo "✨ CI simulation complete (with errors)!"
    exit $KTLINT_EXIT_CODE
fi

if [ "$FORMAT_MODE" = false ]; then
    echo "✅ Report saved to: $REPORT_PATH"
fi
echo "✨ CI simulation complete!"
