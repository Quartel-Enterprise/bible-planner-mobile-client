#!/bin/bash

# Runs one shard of the Compose UI tests, with any Gradle arguments after the shard:
#   ./scripts/ui_test_shard.sh connected 0 3 --continue --max-workers=1
# The ui-tests workflow runs every Android shard on its own emulator. Modules are discovered by
# their src/androidDeviceTest directory, the same one that makes build-logic run their commonTest
# on a device, so a new module joins a shard without anyone editing the workflow.
#
# Shard 0 is :shared's. Its end-to-end flows drive the whole app from launch and are by far the
# longest module on the device, several times any screen module, so no layout can make the job
# finish sooner than :shared alone takes. The screen modules are dealt round-robin over the other
# shards; together they take longer than :shared, so the layout pays off from three shards on. With
# a single shard, everything runs in it.
#
#   desktop    runs the shard's tests on the JVM; pass -PuiTests=only to keep only the UI ones
#   assemble   builds the test APKs of the shard, so it can run before the emulator exists
#   connected  runs the shard's tests on the connected device
#   ios        runs the shard's tests on the iOS simulator, except :shared's, whose end-to-end
#              flows switch tabs through Calf's native UITabBar and leave iOS nothing to run
#
# The script hands Gradle the tasks itself rather than printing them: a shard with no module is an
# error, and a command substitution around a printed list would hide it from ./gradlew, which then
# runs `help` and passes without a test. Add -m to see the tasks without running them.
#
# The task names end in AndroidDeviceTest on purpose: build-logic raises the minSdk of these
# modules only in a build that asks for such a task (see ComposeUiTests.kt).

set -euo pipefail

usage() {
    echo "Usage: $0 <desktop|assemble|connected|ios> <shard-index> <shard-count> [gradle arguments]" >&2
    exit 1
}

[ "$#" -ge 3 ] || usage
case "$1" in desktop | assemble | connected | ios) ;; *) usage ;; esac
for NUMBER in "$2" "$3"; do
    case "$NUMBER" in *[!0-9]* | "") usage ;; esac
done
[ "$2" -lt "$3" ] || usage

MODE="$1"
SHARD_INDEX="$2"
SHARD_COUNT="$3"
shift 3
END_TO_END_DIR="shared"

# The working directory stays the caller's, so a relative path among the Gradle arguments means
# what it would with ./gradlew. A fixed locale, so a shard holds the same modules on the runner and
# on a developer's machine.
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ALL_MODULE_DIRS=$(find "$PROJECT_ROOT" -type d -path '*/src/androidDeviceTest' -not -path '*/build/*' \
    -not -path "$PROJECT_ROOT/.claude/*" \
    | sed -E "s#^$PROJECT_ROOT/##; s#/src/androidDeviceTest\$##" \
    | LC_ALL=C sort)

MODULE_DIRS=$(awk -v e2e="$END_TO_END_DIR" -v shard="$SHARD_INDEX" -v count="$SHARD_COUNT" '
    count == 1 { print; next }
    $0 == e2e { if (shard == 0) print; next }
    { screen++; if (shard > 0 && (screen - 1) % (count - 1) == shard - 1) print }
' <<< "$ALL_MODULE_DIRS")

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

if [ -z "$TASKS" ]; then
    echo "Shard $SHARD_INDEX of $SHARD_COUNT has no module to run in mode $MODE" >&2
    exit 1
fi

# shellcheck disable=SC2086 # the tasks are meant to be split
exec "$PROJECT_ROOT/gradlew" $TASKS "$@"
