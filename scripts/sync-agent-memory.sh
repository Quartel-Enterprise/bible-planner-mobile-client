#!/bin/bash

# Commits the agent memories and pushes them to the private repository
# PierreVieira/bible-planner-agent-memory, which the code review on GitHub reads.
# Usage: scripts/sync-agent-memory.sh "<commit message>"

set -e

memory=$("$(dirname "$0")/agent-memory-dir.sh")

if ! git -C "$memory" rev-parse --git-dir > /dev/null 2>&1; then
    echo "No memory repository at $memory" >&2
    exit 1
fi

git -C "$memory" add -A
git -C "$memory" diff --cached --quiet || git -C "$memory" commit -q -m "${1:-Update memories}"
# Rebased first, so a push from another machine never leaves this one rejected for good
git -C "$memory" pull -q --rebase
git -C "$memory" push -q
echo "Memories synced to $(git -C "$memory" remote get-url origin)"
