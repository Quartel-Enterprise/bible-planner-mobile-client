#!/usr/bin/env bash
# Posts the findings of a Claude review as one pull request review, so GitHub sends one notification
# and the comments arrive together, instead of one review per finding. Usage:
#
#   PR_HEAD_SHA=<reviewed head> PR_BASE_SHA=<its base> post-review.sh <pr-number> <<'JSON'
#   [{"file": "a/b.kt", "line": 12, "summary": "...", "failure_scenario": "...", "category": "correctness"}]
#   JSON
#
#   STARTED=<when the review started> post-review.sh --already <pr-number>
#
# The second form only looks for a review this script posted on the reviewed commit since the given
# time (the one test the hook and the action's posting step share) and prints its url, exiting 1 when
# there is none.
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
#
# FIXED_IN names the commit a review with fix pushed (commit-fixes.sh). The findings marked
# "outcome": "fixed" are then listed at the end of the review body, under that commit, and open no
# thread: there is nothing left to resolve. Without it, every finding is posted the usual way, its
# outcome ignored.
set -euo pipefail

mode=post
if [ "${1:-}" = --already ]; then
  mode=already
  shift
fi
pr=$1
repo=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is not set}
run_url=${RUN_URL:-${GITHUB_SERVER_URL:-}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID:-}}
[ -n "${GITHUB_RUN_ID:-}" ] || run_url=""
head_sha=${PR_HEAD_SHA:-}
base_sha=${PR_BASE_SHA:-}
# Two minutes early: the runner's clock may be ahead of GitHub's, and a review that GitHub took just
# before has to count (see post).
started=$(date -u -d '2 minutes ago' +%Y-%m-%dT%H:%M:%SZ 2> /dev/null || date -u -v-2M +%Y-%m-%dT%H:%M:%SZ)
export WORKSPACE=${GITHUB_WORKSPACE:-}
export FIXED_IN=${FIXED_IN:-}

warn() { echo "::warning::$*" >&2; }

# The reviewed commits, when the caller didn't name them: the pull request's current head and base.
# Known before anything is posted, so every review carries its commit and can be recognised later.
if [ -z "$head_sha" ] || [ -z "$base_sha" ]; then
  if shas=$(gh pr view "$pr" --repo "$repo" --json headRefOid,baseRefOid --jq '.headRefOid + " " + .baseRefOid' 2>&1); then
    read -r current_head current_base <<< "$shas"
    head_sha=${head_sha:-$current_head}
    base_sha=${base_sha:-$current_base}
  else
    warn "The pull request's commits couldn't be read ($(tr '\n' ' ' <<< "$shas" | cut -c1-200))."
  fi
fi

# Prints the url of a review this script posted on the reviewed commit since $1, if any.
already_posted() {
  local url
  [ -n "$head_sha" ] || return 1
  url=$(HEAD_SHA="$head_sha" SINCE="$1" gh api --paginate "repos/$repo/pulls/$pr/reviews" \
    --jq '.[] | select(.commit_id == env.HEAD_SHA and .submitted_at >= env.SINCE
      and (.body | startswith("### Code review"))) | .html_url' 2> /dev/null | tail -1) || return 1
  [ -n "$url" ] && echo "$url"
}

if [ "$mode" = already ]; then
  already_posted "${STARTED:?STARTED is not set}"
  exit $?
fi
input=$(cat)

# Posts the review payload on stdin and prints its url. Transient errors are retried, but creating a
# review isn't idempotent: before each retry, a review of this script's kind on the reviewed commit
# since the script started means GitHub took the request whose answer was lost, and counts as posted.
# A 422, GitHub refusing the payload, returns 22 right away so the caller can post the findings
# another way; a body-only review refused with its commit (gone after a force-push) is posted once
# more on the pull request's current head instead.
post() {
  local payload attempt out already
  payload=$(jq -c --arg sha "$head_sha" '. + (if $sha != "" then {commit_id: $sha} else {} end)')
  for attempt in 1 2 3; do
    if out=$(gh api --method POST "repos/$repo/pulls/$pr/reviews" --input - --jq .html_url <<< "$payload" 2>&1); then
      echo "$out"
      return 0
    fi
    if grep -q 'HTTP 422' <<< "$out"; then
      if jq -e 'has("commit_id") and (has("comments") | not)' <<< "$payload" > /dev/null; then
        warn "GitHub refused the review on the reviewed commit, so it was posted on the pull request's current head."
        payload=$(jq -c 'del(.commit_id)' <<< "$payload")
        continue
      fi
      echo "$out" >&2
      return 22
    fi
    [ "$attempt" -lt 3 ] && sleep $((attempt * 5))
    if already=$(already_posted "$started"); then
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
    path: ((.path // .file)
      | if type == "string" then (if env.WORKSPACE != "" then ltrimstr(env.WORKSPACE + "/") else . end) | ltrimstr("./") else null end
      | if . == "" then null else . end),
    line: ((.line // .end_line) | tonumber? // null),
    start_line: ((.start_line // null) | tonumber? // null),
    body: ((.body // (
      "**" + (.summary // "Finding" | tostring) + "**"
      + (if .failure_scenario then "\n\n" + (.failure_scenario | tostring) else "" end)
      + (if .category then "\n\n_" + (.category | tostring) + "_" else "" end))) | tostring),
    fixed: (env.FIXED_IN != "" and .outcome == "fixed")
  }]' <<< "$input")
total=$(jq 'length' <<< "$findings")

if [ "$total" -eq 0 ]; then
  jq -n '{event: "COMMENT", body: "### Code review\n\nNo findings."}' | post
  exit 0
fi

as_list() {
  jq -r 'to_entries | map("\(.key + 1). " + (if .value.path then "`\(.value.path)" + (if .value.line then ":\(.value.line)" else "" end) + "`" else "(no file)" end)
    + "\n\n\(.value.body)") | join("\n\n")'
}

fixed=$(jq -c 'map(select(.fixed))' <<< "$findings")
fixed_count=$(jq 'length' <<< "$fixed")
findings=$(jq -c 'map(select(.fixed | not))' <<< "$findings")
left=$(jq 'length' <<< "$findings")
lead="$total finding(s)"
fixed_section=""
if [ "$fixed_count" -gt 0 ]; then
  if [ "$left" -eq 0 ]; then
    body="### Code review

$total finding(s), all fixed in $FIXED_IN:

$(as_list <<< "$fixed")"
    jq -n --arg body "$body" '{event: "COMMENT", body: $body}' | post
    exit 0
  fi
  lead="$total finding(s): $fixed_count fixed in $FIXED_IN, listed at the end, and $left left"
  fixed_section="

Fixed in $FIXED_IN:

$(as_list <<< "$fixed")"
fi

# The lines GitHub can anchor a comment to: the new-file lines of the diff hunks between the base
# and the reviewed head. Without a readable diff, every finding goes in the body.
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
  map(. as $f
    | if $f.line == null or $f.path == null then . + {anchored: false}
      else hunk($f.path; $f.line) as $h
      | if $h != null then
          (if .start_line != null and (.start_line >= .line or .start_line < $h.from) then .start_line = null else . end)
          + {anchored: true}
        else nearest($f.path; $f.line) as $n
        | if $n == null then . + {anchored: false}
          else {path, line: (if $f.line < $n.from then $n.from else $n.to end), start_line: null,
                body: ("_On line \($f.line), outside the diff; anchored to the nearest changed line._\n\n" + .body),
                anchored: true} end
        end
      end)
  | {inline: map(select(.anchored)), body: map(select(.anchored | not))}' <<< "$findings")
inline=$(jq -c '.inline' <<< "$split")
outside=$(jq -c '.body' <<< "$split")
inline_count=$(jq 'length' <<< "$inline")

if [ "$inline_count" -gt 0 ]; then
  summary="### Code review

$lead"
  if [ "$inline_count" -eq "$left" ]; then
    summary="$summary, as inline comments."
  else
    summary="$summary: $inline_count as inline comments, the others below, in files outside the diff (they open no thread, so they don't hold the merge).

$(as_list <<< "$outside")"
  fi
  summary="$summary$fixed_section"
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

$lead, on lines GitHub can't anchor a comment to:

$(as_list <<< "$findings")$fixed_section"
jq -n --arg body "$body" '{event: "COMMENT", body: $body}' | post
