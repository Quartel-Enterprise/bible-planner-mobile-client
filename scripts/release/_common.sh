#!/usr/bin/env bash
#
# Shared helpers for the release scripts in this directory.
# Sourced — not executed directly.

REPO="Quartel-Enterprise/bible-planner-mobile-client"
WORKFLOW="release.yml"

# Dispatches the release workflow with the given inputs, then waits for the
# new run to appear and prints its URL.
#
# Args: version platforms track complete_android_release submit_ios_for_review
dispatch_release() {
  local version="$1"
  local platforms="$2"
  local track="$3"
  local complete_android="$4"
  local submit_ios="$5"

  local before
  before="$(gh run list --workflow="$WORKFLOW" --repo "$REPO" --limit 1 --json databaseId --jq '.[0].databaseId // 0')"

  gh workflow run "$WORKFLOW" --repo "$REPO" \
    -f version="$version" \
    -f platforms="$platforms" \
    -f track="$track" \
    -f complete_android_release="$complete_android" \
    -f submit_ios_for_review="$submit_ios"

  echo
  echo "Workflow dispatched. Locating the run..."

  local url=""
  local id
  for _ in $(seq 1 20); do
    sleep 3
    id="$(gh run list --workflow="$WORKFLOW" --repo "$REPO" --limit 1 --json databaseId --jq '.[0].databaseId // empty')"
    if [ -n "$id" ] && [ "$id" != "$before" ]; then
      url="$(gh run list --workflow="$WORKFLOW" --repo "$REPO" --limit 1 --json url --jq '.[0].url')"
      break
    fi
  done

  echo
  if [ -n "$url" ]; then
    echo "Run: $url"
  else
    echo "Dispatched. See: https://github.com/$REPO/actions/workflows/$WORKFLOW"
  fi
}
