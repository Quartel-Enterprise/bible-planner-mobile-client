#!/usr/bin/env bash
# Posts the findings of a Claude review as one pull request review, so GitHub sends one notification
# and the comments arrive together, instead of one review per finding. Usage:
#
#   post-review.sh <pr-number> <<'JSON'
#   [{"file": "a/b.kt", "line": 12, "summary": "...", "failure_scenario": "...", "category": "correctness"}]
#   JSON
#
# The objects are the ones the /code-review skill returns; "path" is read in place of "file" and
# "body" in place of the summary and failure scenario. "line" is the line in the new version of the
# file; "start_line" makes the comment span lines. Both have to be in the pull request's diff. GitHub
# refuses the whole review when one of them isn't, and the findings are then posted as one review
# body with no inline comments, so none is lost. An empty array posts a review saying nothing was
# found, so the pull request shows the review ran.
set -euo pipefail

pr=$1
repo=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is not set}
findings=$(jq -c '[.[] | {
    path: (.path // .file),
    line: (.line // .end_line),
    start_line: (.start_line // null),
    body: (.body // (
      "**" + (.summary // "Finding") + "**"
      + (if .failure_scenario then "\n\n" + .failure_scenario else "" end)
      + (if .category then "\n\n_" + .category + "_" else "" end)))
  }]')
count=$(jq 'length' <<< "$findings")

post() {
  gh api --method POST "repos/$repo/pulls/$pr/reviews" --input - --jq .html_url
}

if [ "$count" -eq 0 ]; then
  jq -n '{event: "COMMENT", body: "### Code review\n\nNo findings."}' | post
  exit 0
fi

summary="### Code review

$count finding(s), as inline comments."
comments=$(jq -c '[.[] | {path, line, side: "RIGHT", body}
  + (if .start_line and .start_line < .line then {start_line, start_side: "RIGHT"} else {} end)]' <<< "$findings")
error=$(mktemp)
if url=$(jq -n --arg body "$summary" --argjson comments "$comments" \
    '{event: "COMMENT", body: $body, comments: $comments}' | post 2> "$error"); then
  echo "$url"
  exit 0
fi

echo "::warning::GitHub refused the inline comments ($(tr '\n' ' ' < "$error" | cut -c1-300)), so the findings were posted as one review body." >&2
body=$(jq -r '"### Code review\n\nGitHub refused the inline comments, so here are the findings:\n\n"
  + (to_entries | map("\(.key + 1). `\(.value.path):\(.value.line)`\n\n\(.value.body)") | join("\n\n"))' <<< "$findings")
jq -n --arg body "$body" '{event: "COMMENT", body: $body}' | post
