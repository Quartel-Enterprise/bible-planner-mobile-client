#!/bin/bash

# Prints Claude Code's memory folder for this repository. Every worktree shares the main checkout's
# folder, named after its path with each character that isn't a letter or a digit turned into '-'.

set -e

main=$(dirname "$(git rev-parse --path-format=absolute --git-common-dir)")
echo "$HOME/.claude/projects/$(printf '%s' "$main" | sed 's/[^A-Za-z0-9]/-/g')/memory"
