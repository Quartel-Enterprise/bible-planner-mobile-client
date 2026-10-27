#!/usr/bin/env bash
# Commits the fixes a Claude review applied to the working tree and pushes the commit to the pull
# request's branch, then prints the commit. Usage:
#
#   commit-fixes.sh <<'JSON'
#   [{"file": "a/b.kt", "line": 12, "summary": "...", "outcome": "fixed"}]
#   JSON
#
# The Stop hook runs it inside the session (see action.yml), where origin's url carries the Claude
# GitHub App's token the action set up: a push with it starts the pull request's workflows on the
# new commit, which one with GITHUB_TOKEN wouldn't, and review-gate lets them run there. The App has
# no workflows permission, so a change under .github would be refused by GitHub anyway; it is
# refused here first, with the rest of the fixes, and so is a change under .claude, whose settings
# and hooks run in every Claude session of the repository. Only the files the branch already tracks
# are committed: the review can't create a file, so an untracked one means a fix that would be
# pushed without it, and nothing is pushed. Git's hooks and fsmonitor are turned off: the working
# tree is the pull request's, and the model edited it.
#
# Nothing is pushed, and the script exits 1, when no finding is marked fixed, when the working tree
# has no change or an untracked file, when the checkout is no longer the reviewed head, or when the
# push is refused (the branch moved during the review). Only the error lines of a failed push reach
# the log, with any credentials in a url masked, since origin's url holds the token; the paths git
# names are the repository's.
set -euo pipefail

findings=$(cat)
cd "${GITHUB_WORKSPACE:?GITHUB_WORKSPACE is not set}"
event=${GITHUB_EVENT_PATH:?GITHUB_EVENT_PATH is not set}
branch=$(jq -r '.pull_request.head.ref // ""' "$event")
head_sha=$(jq -r '.pull_request.head.sha // ""' "$event")

safe_git() { git -c core.hooksPath=/dev/null -c core.fsmonitor=false "$@"; }
fail() {
  echo "commit-fixes: $*" >&2
  exit 1
}

fixed=$(jq -c '[.[]? | select(type == "object" and .outcome == "fixed")]' <<< "$findings" 2> /dev/null) || fixed='[]'
[ "$(jq 'length' <<< "$fixed")" -gt 0 ] || fail "no finding is marked fixed"
[ -n "$branch" ] && [ -n "$head_sha" ] || fail "the event names no pull request branch"
[ "$(safe_git rev-parse HEAD)" = "$head_sha" ] || fail "the checkout is no longer the reviewed head"

# The review can't delete a file, so a tracked file missing from the working tree is a symlink the
# action removed before the review (action.yml): it is put back before anything is committed.
if ! restored=$(safe_git ls-files -d -z |
  xargs -0 -r git -c core.hooksPath=/dev/null -c core.fsmonitor=false checkout -- 2>&1); then
  fail "the files removed before the review couldn't be restored: $(tr '\n' ' ' <<< "$restored")"
fi

changed=$(safe_git diff --name-only)
[ -n "$changed" ] || fail "the working tree has no change"
if grep -qE '^\.(github|claude)/' <<< "$changed"; then
  fail "the fixes change files under .github or .claude"
fi
untracked=$(safe_git ls-files --others --exclude-standard)
if [ -n "$untracked" ]; then
  fail "the working tree has untracked files: $(tr '\n' ' ' <<< "$untracked")"
fi

# The fixes and their summaries become a public commit, so they get the review's mask
# (post-review.sh): the summaries are masked, and a fix that adds a token-shaped string pushes
# nothing. Only the added lines count: a sample token already in the file next to a fix isn't new.
redact() { "$(dirname "$0")/post-review.sh" --redact; }
added=$(safe_git diff -U0 | grep '^+' | grep -v '^+++' || true)
[ "$(redact <<< "$added")" = "$added" ] || fail "the fixes add a token-shaped string"

message="fix: apply the Claude review findings

$(jq -r '.[] | "- " + (.summary // "Finding" | tostring | gsub("\\s+"; " "))' <<< "$fixed" | redact)"
safe_git commit --all --no-verify --quiet --message "$message" > /dev/null 2>&1 || fail "git commit failed"

# A plain push, never forced: a branch that moved during the review keeps its commits.
error=""
for attempt in 1 2; do
  if error=$(safe_git push --quiet origin "HEAD:refs/heads/$branch" 2>&1 > /dev/null); then
    safe_git rev-parse HEAD
    exit 0
  fi
  [ "$attempt" -lt 2 ] && sleep 5
done
reason=$(sed -E 's#://[^/@[:space:]]+@#://***@#g' <<< "$error" | { grep -E '^(remote:|error:|fatal:| ?!)' || true; } | tail -n 3 | tr '\n' ' ')
fail "git push failed: ${reason:-no error from git}"
