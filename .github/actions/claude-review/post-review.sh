#!/usr/bin/env bash
# Posts the findings of a Claude review as one pull request review, so GitHub sends one notification
# and the comments arrive together, instead of one review per finding. Usage:
#
#   RUN_URL=<url of the run> post-review.sh <pr-number> <<'JSON'
#   [{"file": "a/b.kt", "line": 12, "summary": "...", "failure_scenario": "...", "category": "correctness"}]
#   JSON
#
# The objects are the ones the /code-review skill returns; "path" is read in place of "file" and
# "body" in place of the summary and failure scenario. "line" is the line in the new version of the
# file; "start_line" makes the comment span lines. GitHub only anchors a comment to a line inside the
# pull request's diff, and refuses the whole review when one isn't, so the findings on other lines
# (the recipe reports bugs on unchanged lines of a touched function on purpose) go in the review body
# instead, and the rest stay inline. An empty array posts a review saying nothing was found, and
# anything that isn't an array of findings posts a notice, so the pull request always shows the
# review ran. Only a GitHub error that outlasts the retries fails the script.
set -euo pipefail

pr=$1
repo=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is not set}
run_url=${RUN_URL:-${GITHUB_SERVER_URL:-}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID:-}}
[ -n "${GITHUB_RUN_ID:-}" ] || run_url=""
input=$(cat)

# Posts the review payload on stdin and prints its url. Transient errors are retried; a 422, GitHub
# refusing the payload, returns 22 right away so the caller can post the findings another way.
post() {
  local payload attempt out
  payload=$(cat)
  for attempt in 1 2 3; do
    if out=$(gh api --method POST "repos/$repo/pulls/$pr/reviews" --input - --jq .html_url <<< "$payload" 2>&1); then
      echo "$out"
      return 0
    fi
    if grep -q 'HTTP 422' <<< "$out"; then
      echo "$out" >&2
      return 22
    fi
    [ "$attempt" -lt 3 ] && sleep $((attempt * 15))
  done
  echo "$out" >&2
  return 1
}

notice() {
  echo "::warning::$1, so only a notice was posted." >&2
  jq -n --arg why "$1" --arg url "$run_url" '{event: "COMMENT", body: ("### Code review\n\n" + $why
    + (if $url != "" then "; see [the run](" + $url + ")." else "." end))}' | post
}

if ! findings=$(jq -ce 'select(type == "array")
    | select(all(.[]; type == "object"
      and ((.path // .file) | type) == "string"
      and ((.line // .end_line) | type) == "number"))' <<< "$input" 2> /dev/null); then
  notice "The review ended without a findings list"
  exit 0
fi

findings=$(jq -c '[.[] | {
    path: (.path // .file),
    line: (.line // .end_line),
    start_line: (.start_line // null),
    body: (.body // (
      "**" + (.summary // "Finding") + "**"
      + (if .failure_scenario then "\n\n" + .failure_scenario else "" end)
      + (if .category then "\n\n_" + .category + "_" else "" end)))
  }]' <<< "$findings")

if [ "$(jq 'length' <<< "$findings")" -eq 0 ]; then
  jq -n '{event: "COMMENT", body: "### Code review\n\nNo findings."}' | post
  exit 0
fi

# The lines GitHub can anchor a comment to: the new-file lines of the pull request's diff hunks.
hunks=$(gh pr diff "$pr" --repo "$repo" | awk '
  /^\+\+\+ / { path = substr($2, 3); next }
  /^@@ / { split($3, range, ","); start = substr(range[1], 2); length_ = (range[2] == "" ? 1 : range[2])
           if (length_ > 0) printf "%s\t%d\t%d\n", path, start, start + length_ - 1 }' |
  jq -R -s -c '[split("\n")[] | select(. != "") | split("\t") | {path: .[0], from: (.[1] | tonumber), to: (.[2] | tonumber)}]')

# A finding whose line is in a hunk is an inline comment; one whose start line isn't loses the span.
split=$(jq -c --argjson hunks "$hunks" '
  def covers($path; $line): any($hunks[]; .path == $path and .from <= $line and $line <= .to);
  map(if .start_line != null and (.start_line >= .line or (covers(.path; .start_line) | not))
      then .start_line = null else . end)
  | {inline: map(select(covers(.path; .line))), body: map(select(covers(.path; .line) | not))}' <<< "$findings")
inline=$(jq -c '.inline' <<< "$split")
outside=$(jq -c '.body' <<< "$split")
total=$(jq 'length' <<< "$findings")
inline_count=$(jq 'length' <<< "$inline")

as_list() {
  jq -r 'to_entries | map("\(.key + 1). `\(.value.path):\(.value.line)`\n\n\(.value.body)") | join("\n\n")'
}

if [ "$inline_count" -gt 0 ]; then
  summary="### Code review

$total finding(s)"
  if [ "$inline_count" -eq "$total" ]; then
    summary="$summary, as inline comments."
  else
    summary="$summary: $inline_count as inline comments, the others below, on lines outside the diff.

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
  echo "::warning::GitHub refused the inline comments, so the findings were posted as one review body." >&2
fi

body="### Code review

$total finding(s), on lines GitHub can't anchor a comment to:

$(as_list <<< "$findings")"
jq -n --arg body "$body" '{event: "COMMENT", body: $body}' | post
