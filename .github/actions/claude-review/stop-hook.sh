#!/usr/bin/env bash
# Stop hook of the review session (see action.yml): when Claude's reply ends, posts the findings it
# holds as one pull request review, through post-review.sh. It runs inside the session, so gh posts
# with the Claude GitHub App's token, as claude[bot], before the action revokes it. The hook's JSON
# arrives on stdin; the transcript it names holds the reply. A marker file tells the action's own
# posting step the review is already posted; a log file tells it what the hook saw, since the
# session's output is hidden. When the hook finds no findings list, it posts nothing and leaves the
# posting to that step, which reads the reply from the action's execution file instead. Whatever
# happens, the hook exits 0: it must never block the session from ending.
set -uo pipefail

dir=$(cd "$(dirname "$0")" && pwd)
log="$dir/.hook-log"
note() { echo "stop-hook: $*" | tee -a "$log" >&2; }

transcript=$(jq -r '.transcript_path // ""')
if [ -z "$transcript" ] || [ ! -f "$transcript" ]; then
  note "no transcript to read the findings from (${transcript:-no path})"
  exit 0
fi
note "transcript: $(wc -l < "$transcript") lines"

# The runner's environment reaches the session: the pull request number comes from the ref the
# workflow checked out (refs/pull/<number>/merge).
pr=${PR_NUMBER:-$(sed -n 's#^refs/pull/\([0-9]*\)/merge$#\1#p' <<< "${GITHUB_REF:-}")}
if [ -z "$pr" ]; then
  note "no pull request number (PR_NUMBER or GITHUB_REF)"
  exit 0
fi

# The findings are in the last assistant message, or in one just before it when the model added a
# closing line: the last five messages with text are tried, newest first.
replies=$(jq -rs '[.[] | select(.type == "assistant")
  | .message.content
  | if type == "array" then map(select(.type == "text") | .text) | join("\n") else tostring end
  | select(length > 0)] | reverse | .[0:5] | .[] | @base64' "$transcript")
note "assistant messages with text tried: $(grep -c . <<< "$replies")"

findings=""
while read -r encoded; do
  [ -n "$encoded" ] || continue
  reply=$(base64 -d <<< "$encoded")
  findings=$(python3 "$dir/extract-findings.py" <<< "$reply")
  if [ -n "$findings" ]; then
    note "findings list found: $(jq 'length' <<< "$findings") finding(s), reply of ${#reply} characters"
    break
  fi
  note "no findings list in a reply of ${#reply} characters starting with: $(head -c 120 <<< "$reply" | tr '\n' ' ')"
done <<< "$replies"

if [ -z "$findings" ]; then
  note "no findings list in the transcript, the action's posting step takes over"
  exit 0
fi

if url=$("$dir/post-review.sh" "$pr" <<< "$findings"); then
  echo "$url" > "$dir/.posted"
  note "posted $url"
else
  note "post-review.sh failed, the action's posting step takes over"
fi
exit 0
