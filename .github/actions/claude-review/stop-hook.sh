#!/usr/bin/env bash
# Stop hook of the review session (see action.yml): when Claude's reply ends, posts the findings it
# holds as one pull request review, through post-review.sh. It runs inside the session, so gh posts
# with the Claude GitHub App's token, as claude[bot], before the action revokes it. The hook's JSON
# arrives on stdin with the last assistant message; the transcript it names is the fallback, though
# it may not hold that message yet. A marker file tells the action's own posting step the review is
# already posted; a log file tells it what the hook saw (counts and sizes only: the session's output
# is hidden because the logs are public, and the log must not leak it). When the hook finds no
# findings list, it posts nothing and leaves the posting to that step, which reads the reply from the
# action's execution file instead. Whatever happens, the hook exits 0: it must never block the session
# from ending.
set -uo pipefail

dir=$(cd "$(dirname "$0")" && pwd)
log="$dir/.hook-log"
note() { echo "stop-hook: $*" | tee -a "$log" >&2; }

input=$(cat)

# One review per session: a Stop that fires again posts nothing more.
if [ -f "$dir/.posted" ]; then
  note "the review is already posted ($(cat "$dir/.posted")), nothing more to do"
  exit 0
fi

# The runner's environment reaches the session: the pull request number comes from the ref the
# workflow checked out (refs/pull/<number>/merge), and the reviewed commits from the event payload.
pr=${PR_NUMBER:-$(sed -n 's#^refs/pull/\([0-9]*\)/merge$#\1#p' <<< "${GITHUB_REF:-}")}
if [ -z "$pr" ]; then
  note "no pull request number (PR_NUMBER or GITHUB_REF)"
  exit 0
fi
if [ -z "${PR_HEAD_SHA:-}" ] && [ -n "${GITHUB_EVENT_PATH:-}" ] && [ -f "$GITHUB_EVENT_PATH" ]; then
  PR_HEAD_SHA=$(jq -r '.pull_request.head.sha // ""' "$GITHUB_EVENT_PATH")
  PR_BASE_SHA=$(jq -r '.pull_request.base.sha // ""' "$GITHUB_EVENT_PATH")
  export PR_HEAD_SHA PR_BASE_SHA
fi

replies=$(jq -r '.last_assistant_message // "" | select(length > 0) | @base64' <<< "$input")
if [ -n "$replies" ]; then
  note "reading the last assistant message from the hook input"
else
  transcript=$(jq -r '.transcript_path // ""' <<< "$input")
  if [ -z "$transcript" ] || [ ! -f "$transcript" ]; then
    note "no last assistant message and no transcript (${transcript:-no path})"
    exit 0
  fi
  # The last five messages with text, newest first: the transcript may lag behind the reply.
  replies=$(jq -rs '[.[] | select(.type == "assistant")
    | .message.content
    | if type == "array" then map(select(.type == "text") | .text) | join("\n") else tostring end
    | select(length > 0)] | reverse | .[0:5] | .[] | @base64' "$transcript")
  note "reading the transcript ($(wc -l < "$transcript") lines, $(grep -c . <<< "$replies") assistant messages with text tried)"
fi

findings=""
while read -r encoded; do
  [ -n "$encoded" ] || continue
  reply=$(base64 -d <<< "$encoded")
  findings=$(python3 "$dir/extract-findings.py" <<< "$reply")
  if [ -n "$findings" ]; then
    note "findings list found: $(jq 'length' <<< "$findings") finding(s), in a reply of ${#reply} characters"
    break
  fi
  note "no findings list in a reply of ${#reply} characters"
done <<< "$replies"

if [ -z "$findings" ]; then
  note "no findings list, the action's posting step takes over"
  exit 0
fi

# A review posted since the review started (a Stop that fired twice without the marker) counts too.
if [ -f "$dir/.started" ] && url=$(STARTED="$(cat "$dir/.started")" "$dir/post-review.sh" --already "$pr"); then
  echo "$url" > "$dir/.posted"
  note "a review was already posted since the review started: $url"
  exit 0
fi

if url=$("$dir/post-review.sh" "$pr" <<< "$findings"); then
  echo "$url" > "$dir/.posted"
  note "posted $url"
else
  note "post-review.sh failed, the action's posting step takes over"
fi
exit 0
