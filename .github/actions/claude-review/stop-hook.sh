#!/usr/bin/env bash
# Stop hook of the review session (see action.yml): when Claude's reply ends, posts the findings it
# holds as one pull request review, through post-review.sh. It runs inside the session, so gh posts
# with the Claude GitHub App's token, as claude[bot], before the action revokes it. The hook's JSON
# arrives on stdin; the transcript it names holds the reply. A marker file tells the action's own
# posting step the review is already posted. Whatever happens, the hook exits 0: it must never block
# the session from ending.
set -uo pipefail

dir=$(cd "$(dirname "$0")" && pwd)
transcript=$(jq -r '.transcript_path // ""')
if [ -z "$transcript" ] || [ ! -f "$transcript" ]; then
  echo "stop-hook: no transcript to read the findings from" >&2
  exit 0
fi

reply=$(jq -rs '[.[] | select(.type == "assistant")
  | .message.content
  | if type == "array" then map(select(.type == "text") | .text) | join("\n") else tostring end
  | select(length > 0)] | last // ""' "$transcript")

# The runner's environment reaches the session: the pull request number comes from the ref the
# workflow checked out (refs/pull/<number>/merge).
pr=${PR_NUMBER:-$(sed -n 's#^refs/pull/\([0-9]*\)/merge$#\1#p' <<< "${GITHUB_REF:-}")}
if [ -z "$pr" ]; then
  echo "stop-hook: no pull request number (PR_NUMBER or GITHUB_REF)" >&2
  exit 0
fi

if url=$(python3 "$dir/extract-findings.py" <<< "$reply" | "$dir/post-review.sh" "$pr"); then
  echo "$url" > "$dir/.posted"
  echo "stop-hook: posted $url" >&2
else
  echo "stop-hook: post-review.sh failed, the action's posting step takes over" >&2
fi
exit 0
