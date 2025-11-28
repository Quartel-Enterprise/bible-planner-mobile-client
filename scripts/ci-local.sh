#!/bin/bash

# Script to simulate CI locally
# This runs the same checks that GitHub Actions runs

set -e

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

# Check if Java 21 is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install JDK 21 (Temurin distribution recommended)."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" != "21" ]; then
    echo "⚠️  Warning: Java version is $JAVA_VERSION, but CI uses Java 21"
    echo "   Consider using Java 21 for consistency with CI"
    echo ""
fi

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found in project root: $PROJECT_ROOT"
    exit 1
fi

# Make gradlew executable (if not already)
chmod +x ./gradlew

echo "📋 Running ktlint check..."
echo ""

# Run ktlint check (same as CI)
# Temporarily disable set -e to capture exit code and still collect reports
set +e
./gradlew ktlintCheck --continue
KTLINT_EXIT_CODE=$?
set -e

echo ""
echo "📊 Collecting ktlint reports..."

# Create build directory if it doesn't exist
mkdir -p build

# Find all checkstyle reports from subprojects
REPORTS=$(find . -path "*/build/reports/ktlint/*/*.xml" -type f 2>/dev/null || true)

if [ -n "$REPORTS" ]; then
    # Use the first report found (or we could merge multiple reports)
    FIRST_REPORT=$(echo "$REPORTS" | head -1)
    cp "$FIRST_REPORT" build/ktlint-report.xml
    echo "✅ Report saved to: build/ktlint-report.xml"
    echo "   Source: $FIRST_REPORT"
else
    # Create empty report if none found
    echo '<?xml version="1.0" encoding="UTF-8"?><checkstyle version="4.3"></checkstyle>' > build/ktlint-report.xml
    echo "ℹ️  No ktlint reports found (no issues or no files checked)"
fi

echo ""
if [ $KTLINT_EXIT_CODE -ne 0 ]; then
    echo "❌ ktlint found formatting errors. Please run './gradlew ktlintFormat' to fix them."
    echo ""
    echo "✨ CI simulation complete (with errors)!"
    exit $KTLINT_EXIT_CODE
else
    echo "✨ CI simulation complete!"
fi
echo ""
echo "To fix ktlint issues, run:"
echo "  ./gradlew ktlintFormat"

