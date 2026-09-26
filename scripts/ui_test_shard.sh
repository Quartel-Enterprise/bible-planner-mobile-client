#!/bin/bash

# Prints the Gradle tasks of one shard of the Compose UI tests, for example:
#   ./gradlew $(./scripts/ui_test_shard.sh connected 0 2)
# The ui-tests workflow runs every Android shard on its own emulator. Modules are discovered by
# their src/androidDeviceTest directory, the same one that makes build-logic run their commonTest
# on a device, and dealt out round-robin, so a new module joins a shard without anyone editing the
# workflow.
#
#   desktop    runs the shard's tests on the JVM; pass -PuiTests=only to keep only the UI ones
#   assemble   builds the test APKs of the shard, so it can run before the emulator exists
#   connected  runs the shard's tests on the connected device
#   ios        runs the shard's tests on the iOS simulator
#
# The task names end in AndroidDeviceTest on purpose: build-logic raises the minSdk of these
# modules only in a build that asks for such a task (see ComposeUiTests.kt).

set -euo pipefail

if [ "$#" -ne 3 ] || { [ "$1" != "desktop" ] && [ "$1" != "assemble" ] && [ "$1" != "connected" ] && [ "$1" != "ios" ]; }; then
    echo "Usage: $0 <desktop|assemble|connected|ios> <shard-index> <shard-count>" >&2
    exit 1
fi

MODE="$1"
SHARD_INDEX="$2"
SHARD_COUNT="$3"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

MODULE_DIRS=$(find . -type d -path '*/src/androidDeviceTest' -not -path '*/build/*' -not -path './.claude/*' \
    | sed -E 's#^\./##; s#/src/androidDeviceTest$##' \
    | sort \
    | awk -v shard="$SHARD_INDEX" -v count="$SHARD_COUNT" '(NR - 1) % count == shard')

TASKS=""
for MODULE_DIR in $MODULE_DIRS; do
    MODULE=":${MODULE_DIR//\//:}"
    case "$MODE" in
        desktop) TASKS="$TASKS $MODULE:jvmTest" ;;
        assemble) TASKS="$TASKS $MODULE:assembleAndroidDeviceTest" ;;
        connected) TASKS="$TASKS $MODULE:connectedAndroidDeviceTest" ;;
        ios) TASKS="$TASKS $MODULE:iosSimulatorArm64Test" ;;
    esac
done

echo "${TASKS# }"
