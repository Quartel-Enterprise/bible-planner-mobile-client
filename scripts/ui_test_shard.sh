#!/bin/bash

# Prints the Gradle tasks of one shard of the Compose UI tests, for example:
#   ./gradlew $(./scripts/ui_test_shard.sh connected 0 3)
# The ui-tests workflow runs every Android shard on its own emulator. Modules are discovered by
# their src/androidDeviceTest directory, the same one that makes build-logic run their commonTest
# on a device, so a new module joins a shard without anyone editing the workflow.
#
# Shard 0 is :shared's. Its end-to-end flows drive the whole app from launch, so on the device the
# module takes longer than the screen modules together. Those are dealt round-robin over the other
# shards. With a single shard, everything runs in it.
#
#   desktop    runs the shard's tests on the JVM; pass -PuiTests=only to keep only the UI ones
#   assemble   builds the test APKs of the shard, so it can run before the emulator exists
#   connected  runs the shard's tests on the connected device
#   ios        runs the shard's tests on the iOS simulator, except :shared's, whose end-to-end
#              flows switch tabs through Calf's native UITabBar and leave iOS nothing to run
#
# The task names end in AndroidDeviceTest on purpose: build-logic raises the minSdk of these
# modules only in a build that asks for such a task (see ComposeUiTests.kt).

set -euo pipefail

usage() {
    echo "Usage: $0 <desktop|assemble|connected|ios> <shard-index> <shard-count>" >&2
    exit 1
}

[ "$#" -eq 3 ] || usage
case "$1" in desktop | assemble | connected | ios) ;; *) usage ;; esac
for NUMBER in "$2" "$3"; do
    case "$NUMBER" in *[!0-9]* | "") usage ;; esac
done
[ "$2" -lt "$3" ] || usage

MODE="$1"
SHARD_INDEX="$2"
SHARD_COUNT="$3"
END_TO_END_DIR="shared"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

ALL_MODULE_DIRS=$(find . -type d -path '*/src/androidDeviceTest' -not -path '*/build/*' -not -path './.claude/*' \
    | sed -E 's#^\./##; s#/src/androidDeviceTest$##' \
    | sort)

END_TO_END_DIRS=$(grep -x "$END_TO_END_DIR" <<< "$ALL_MODULE_DIRS" || true)
SCREEN_DIRS=$(grep -vx "$END_TO_END_DIR" <<< "$ALL_MODULE_DIRS" || true)

if [ "$SHARD_COUNT" -eq 1 ]; then
    MODULE_DIRS="$ALL_MODULE_DIRS"
elif [ "$SHARD_INDEX" -eq 0 ]; then
    MODULE_DIRS="$END_TO_END_DIRS"
else
    MODULE_DIRS=$(awk -v shard="$SHARD_INDEX" -v count="$SHARD_COUNT" \
        '(NR - 1) % (count - 1) == shard - 1' <<< "$SCREEN_DIRS")
fi

TASKS=""
for MODULE_DIR in $MODULE_DIRS; do
    MODULE=":${MODULE_DIR//\//:}"
    case "$MODE" in
        desktop) TASKS="$TASKS $MODULE:jvmTest" ;;
        assemble) TASKS="$TASKS $MODULE:assembleAndroidDeviceTest" ;;
        connected) TASKS="$TASKS $MODULE:connectedAndroidDeviceTest" ;;
        ios) [ "$MODULE_DIR" = "$END_TO_END_DIR" ] || TASKS="$TASKS $MODULE:iosSimulatorArm64Test" ;;
    esac
done

# An empty shard would hand ./gradlew no task at all, and Gradle then runs `help` and the job
# passes without a single test. The callers check this exit status: a command substitution's is
# otherwise lost.
if [ -z "$TASKS" ]; then
    echo "Shard $SHARD_INDEX of $SHARD_COUNT has no module to run in mode $MODE" >&2
    exit 1
fi

echo "${TASKS# }"
