#!/usr/bin/env bash
# Posts the findings of a Claude review as one pull request review, so GitHub sends one notification
# and the comments arrive together, instead of one review per finding. Usage:
#
#   PR_HEAD_SHA=<reviewed head> PR_BASE_SHA=<its base> post-review.sh <pr-number> <<'JSON'
#   [{"file": "a/b.kt", "line": 12, "summary": "...", "failure_scenario": "...", "category": "correctness"}]
#   JSON
#
# The objects are the ones the /code-review skill returns; "path" is read in place of "file" and
# "body" in place of the summary and failure scenario. "line" is the line in the new version of the
# file; "start_line" makes the comment span lines. The review is anchored to the reviewed head commit
# (PR_HEAD_SHA, else the pull request's current head), so a push during the review doesn't move the
# comments. GitHub only anchors a comment to a line inside a diff hunk of that commit, and refuses the
# whole review when one isn't. A finding on another line of a changed file (the recipe reports bugs on
# unchanged lines of a touched function on purpose) is anchored to the nearest changed line of that
# file, with its own line named, so it still opens a thread that holds the merge. The findings in
# files the diff doesn't touch, the ones without a usable file and line, and all of them when the diff
# can't be read, go in the review body instead; those open no thread. An empty array posts a review
# saying nothing was found, and anything that isn't an array posts a notice, so the pull request
# always shows the review ran. Only a GitHub error that outlasts the retries fails the script.
set -euo pipefail

pr=$1
repo=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is not set}
run_url=${RUN_URL:-${GITHUB_SERVER_URL:-}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID:-}}
[ -n "${GITHUB_RUN_ID:-}" ] || run_url=""
head_sha=${PR_HEAD_SHA:-}
base_sha=${PR_BASE_SHA:-}
started=$(date -u +%Y-%m-%dT%H:%M:%SZ)
input=$(cat)

warn() { echo "::warning::$*" >&2; }

# Posts the review payload on stdin and prints its url. Transient errors are retried, but creating a
# review isn't idempotent: before each retry, a review of this script's kind on the reviewed commit
# since the script started means GitHub took the request whose answer was lost, and counts as posted.
# A 422, GitHub refusing the payload, returns 22 right away so the caller can post the findings
# another way.
post() {
  local payload attempt out already
  payload=$(jq -c --arg sha "$head_sha" '. + (if $sha != "" then {commit_id: $sha} else {} end)')
  for attempt in 1 2 3; do
    if out=$(gh api --method POST "repos/$repo/pulls/$pr/reviews" --input - --jq .html_url <<< "$payload" 2>&1); then
      echo "$out"
      return 0
    fi
    if grep -q 'HTTP 422' <<< "$out"; then
      echo "$out" >&2
      return 22
    fi
    [ "$attempt" -lt 3 ] && sleep $((attempt * 5))
    already=$(HEAD_SHA="$head_sha" STARTED="$started" gh api --paginate "repos/$repo/pulls/$pr/reviews" \
      --jq 'select(env.HEAD_SHA != "") | .[] | select(.commit_id == env.HEAD_SHA and .submitted_at >= env.STARTED
        and (.body | startswith("### Code review"))) | .html_url' 2> /dev/null | tail -1 || true)
    if [ -n "$already" ]; then
      echo "$already"
      return 0
    fi
  done
  echo "$out" >&2
  return 1
}

if ! jq -e 'type == "array"' <<< "$input" > /dev/null 2>&1; then
  warn "The review ended without a findings list, so only a notice was posted."
  jq -n --arg url "$run_url" '{event: "COMMENT", body: ("### Code review\n\nThe review ended without a findings list"
    + (if $url != "" then "; see [the run](" + $url + ")." else "." end))}' | post
  exit 0
fi

# One shape for every finding; a number given as text still counts.
findings=$(jq -c '[.[] | select(type == "object") | {
    path: ((.path // .file) | if type == "string" and . != "" then . else null end),
    line: ((.line // .end_line) | tonumber? // null),
    start_line: ((.start_line // null) | tonumber? // null),
    body: (.body // (
      "**" + (.summary // "Finding") + "**"
      + (if .failure_scenario then "\n\n" + .failure_scenario else "" end)
      + (if .category then "\n\n_" + .category + "_" else "" end)))
  }]' <<< "$input")
total=$(jq 'length' <<< "$findings")

if [ "$total" -eq 0 ]; then
  jq -n '{event: "COMMENT", body: "### Code review\n\nNo findings."}' | post
  exit 0
fi

# The lines GitHub can anchor a comment to: the new-file lines of the diff hunks between the base
# and the reviewed head. Without a readable diff, every finding goes in the body.
if [ -z "$head_sha" ] || [ -z "$base_sha" ]; then
  if shas=$(gh pr view "$pr" --repo "$repo" --json headRefOid,baseRefOid --jq '.headRefOid + " " + .baseRefOid' 2>&1); then
    read -r head_sha base_sha <<< "$shas"
  else
    warn "The pull request's commits couldn't be read ($(tr '\n' ' ' <<< "$shas" | cut -c1-200)), so the findings were posted in the review body."
  fi
fi
hunks='[]'
if [ -n "$head_sha" ] && [ -n "$base_sha" ]; then
  # shellcheck disable=SC2016 # $file is jq's, not the shell's
  if ! hunks=$(gh api --paginate "repos/$repo/compare/$base_sha...$head_sha?per_page=100" \
      --jq '.files[]? | .filename as $file | (.patch // "")
        | [match("(?m)^@@ -[0-9]+(?:,[0-9]+)? \\+([0-9]+)(?:,([0-9]+))? @@"; "g")]
        | .[] | {path: $file, from: (.captures[0].string | tonumber),
                 to: ((.captures[0].string | tonumber) + ((.captures[1].string // "1") | tonumber) - 1)}
        | select(.to >= .from)' 2> /dev/null | jq -s -c '.'); then
    warn "The diff of the reviewed commit couldn't be read, so the findings were posted in the review body."
    hunks='[]'
  fi
fi

# A finding whose line is in a hunk is an inline comment; a span has to start in that same hunk. One
# on another line of a changed file is anchored to the nearest hunk's edge, naming its own line.
split=$(jq -c --argjson hunks "$hunks" '
  def hunk($path; $line): first($hunks[] | select(.path == $path and .from <= $line and $line <= .to)) // null;
  def nearest($path; $line): [$hunks[] | select(.path == $path)]
    | if length == 0 then null else min_by(if $line < .from then .from - $line else $line - .to end) end;
  map(. as $f | hunk($f.path; $f.line) as $h | nearest($f.path; $f.line) as $n
    | if $h != null then
        (if .start_line != null and (.start_line >= .line or .start_line < $h.from) then .start_line = null else . end)
        + {anchored: true}
      elif $f.line != null and $n != null then
        {path, line: (if $f.line < $n.from then $n.from else $n.to end), start_line: null,
         body: ("_On line \($f.line), outside the diff; anchored to the nearest changed line._\n\n" + .body),
         anchored: true}
      else . + {anchored: false} end)
  | {inline: map(select(.anchored)), body: map(select(.anchored | not))}' <<< "$findings")
inline=$(jq -c '.inline' <<< "$split")
outside=$(jq -c '.body' <<< "$split")
inline_count=$(jq 'length' <<< "$inline")

as_list() {
  jq -r 'to_entries | map("\(.key + 1). " + (if .value.path then "`\(.value.path)" + (if .value.line then ":\(.value.line)" else "" end) + "`" else "(no file)" end)
    + "\n\n\(.value.body)") | join("\n\n")'
}

if [ "$inline_count" -gt 0 ]; then
  summary="### Code review

$total finding(s)"
  if [ "$inline_count" -eq "$total" ]; then
    summary="$summary, as inline comments."
  else
    summary="$summary: $inline_count as inline comments, the others below, in files outside the diff (they open no thread, so they don't hold the merge).

$(as_list <<< "$outside")"
  fi
  comments=$(jq -c '[.[] | {path, line, side: "RIGHT", body}
    + (if .start_line != null then {start_line, start_side: "RIGHT"} else {} end)]' <<< "$inline")
  if url=$(jq -n --arg body "$summary" --argjson comments "$comments" \
      '{event: "COMMENT", body: $body, comments: $comments}' | post); then
    echo "$url"
    exit 0
  elif [ $? -ne 22 ]; then
    exit 1
  fi
  warn "GitHub refused the inline comments, so the findings were posted as one review body."
fi

body="### Code review

$total finding(s), on lines GitHub can't anchor a comment to:

$(as_list <<< "$findings")"
jq -n --arg body "$body" '{event: "COMMENT", body: $body}' | post
