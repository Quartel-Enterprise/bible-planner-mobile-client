#!/usr/bin/env bash
# Posts the findings of a Claude review as one pull request review, so GitHub sends one notification
# and the comments arrive together, instead of one review per finding. Usage:
#
#   PR_HEAD_SHA=<reviewed head> PR_BASE_SHA=<its base> post-review.sh <pr-number> <<'JSON'
#   [{"file": "a/b.kt", "line": 12, "summary": "...", "failure_scenario": "...", "category": "correctness",
#     "severity": "must-fix", "fix": "...", "suggestion": "    return null"}]
#   JSON
#
#   STARTED=<when the review started> post-review.sh --already <pr-number>
#
# The second form only looks for a review this script posted in the same workflow run, on any commit
# (the one test the hook and the action's posting step share), and prints its url, exiting 1 when
# there is none and 2 when GitHub couldn't be read. Each review carries its run in a hidden comment;
# outside a workflow run, a review since the given time counts.
#
# The objects are the ones the /code-review skill returns, plus what the review step adds to them:
# "severity" (must-fix, should-fix or nitpick) labels the comment and orders the findings, "fix" says
# how to fix it, and "suggestion" is the text that replaces the commented lines, posted as a GitHub
# suggestion the author can commit. "path" is read in place of "file", and "body" in place of all
# of these. "line" is the line in the new version of the file; "start_line" makes the comment span
# lines. The review is anchored to the reviewed head commit
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

# Masks anything shaped like a credential before it reaches this public repository: the review's
# shell can read the runner's environment and files, where the Claude OAuth token (sk-ant-...), the
# GitHub tokens (ghs_, ghp_, gho_, ghu_, ghr_, github_pat_), the OIDC request token (a JWT) and
# checkout's Basic header (base64 of x-access-token:...) live, and a prompt injection in the diff
# could ask for one in a finding. Only those shapes are matched, so an otherwise encoded token still
# gets through: forks, whose diffs nobody vetted, are never reviewed. `post-review.sh --redact`
# filters stdin, for the hook's log. A Basic or Bearer value has to hold a digit and 20 characters, so
# prose after the word ("Bearer authentication") stays.
redact() {
  perl -pe 's/sk-ant-[A-Za-z0-9_-]{8,}/[redacted]/g;
    s/(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,})/[redacted]/g;
    s/eyJ[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}/[redacted]/g;
    s/eC1hY2Nlc3MtdG9rZW46[A-Za-z0-9+\/=]*/[redacted]/g;
    s/(?:(?<=\\n)|(?<=\\t)|\b)(basic|bearer) (?=[A-Za-z0-9+\/=_-]*[0-9])[A-Za-z0-9+\/=_-]{20,}/$1 [redacted]/gi'
}

mode=post
case "${1:-}" in
  --already)
    mode=already
    shift
    ;;
  --redact)
    redact
    exit 0
    ;;
esac
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
# Marks the reviews of this workflow run, so another run's review posted meanwhile (a review on
# request next to the opening one) never counts as this one's.
export RUN_TAG=""
[ -z "${GITHUB_RUN_ID:-}" ] || RUN_TAG="<!-- claude-review run ${GITHUB_RUN_ID} attempt ${GITHUB_RUN_ATTEMPT:-1} -->"

warn() { echo "::warning::$*" >&2; }

# Prints the url of a review this script posted in this workflow run (since $1 outside one), if any,
# on any commit: post() moves a review to the current head when GitHub refuses the reviewed one.
# Returns 1 when there is none and 2 when GitHub couldn't be read, so a caller can tell them apart.
already_posted() {
  local url
  url=$(SINCE="$1" gh api --paginate "repos/$repo/pulls/$pr/reviews" \
    --jq '.[] | select((.body | startswith("### Code review"))
      and (.user.login == "claude[bot]" or .user.login == "github-actions[bot]")
      and (if env.RUN_TAG != "" then (.body | contains(env.RUN_TAG)) else .submitted_at >= env.SINCE end))
      | .html_url' 2> /dev/null | tail -1) || return 2
  [ -n "$url" ] && echo "$url"
}

if [ "$mode" = already ]; then
  already_posted "${STARTED:?STARTED is not set}"
  exit $?
fi

# The reviewed commits, when the caller didn't name them: the pull request's current head and base.
# Known before anything is posted, so every review carries its commit.
if [ -z "$head_sha" ] || [ -z "$base_sha" ]; then
  if shas=$(gh pr view "$pr" --repo "$repo" --json headRefOid,baseRefOid --jq '.headRefOid + " " + .baseRefOid' 2>&1); then
    read -r current_head current_base <<< "$shas"
    head_sha=${head_sha:-$current_head}
    base_sha=${base_sha:-$current_base}
  else
    warn "The pull request's commits couldn't be read ($(tr '\n' ' ' <<< "$shas" | cut -c1-200))."
  fi
fi

# The fix commit is named by a marker outside the session's reach, but it only counts when it is the
# checkout's head and its parent is the reviewed commit: the session can't commit. The marker's text
# isn't printed, in case it holds something else.
if [ -n "$FIXED_IN" ] && { [ -z "$WORKSPACE" ] || [ -z "$head_sha" ] ||
  [ "$(git -C "$WORKSPACE" rev-parse HEAD 2> /dev/null)" != "$FIXED_IN" ] ||
  [ "$(git -C "$WORKSPACE" rev-parse HEAD^ 2> /dev/null)" != "$head_sha" ]; }; then
  warn "The .pushed marker doesn't name a fix commit on top of the reviewed commit, so the findings are posted as not fixed."
  FIXED_IN=""
fi
input=$(cat)

# Posts the review payload on stdin and prints its url. Transient errors are retried, but creating a
# review isn't idempotent: before each retry, a review of this run means GitHub took the request whose
# answer was lost, and counts as posted. Every body ends with the run's hidden tag.
# A 422, GitHub refusing the payload, returns 22 right away so the caller can post the findings
# another way; a body-only review refused with its commit (gone after a force-push) is posted once
# more on the pull request's current head instead.
post() {
  local payload attempt out already
  payload=$(jq -c --arg sha "$head_sha" '. + (if $sha != "" then {commit_id: $sha} else {} end)
    | if env.RUN_TAG != "" then .body += "\n\n" + env.RUN_TAG else . end' | redact)
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

# One shape for every finding, most severe first; a number given as text still counts, and a severity
# the script doesn't know counts as should-fix, so every finding is labelled. A span given end first
# loses its start and its suggestion, since the lines the suggestion was written for are unknown, and
# a fixed finding loses its suggestion too. Each finding gets three bodies: "body" holds its suggestion as a
# GitHub suggestion block, which replaces exactly the lines the comment is anchored to, "loose" shows
# it as a plain code block naming the lines it was written for, for a finding anchored elsewhere or
# listed in the review body, and "short" leaves it out, for a review body too long otherwise.
# shellcheck disable=SC2016 # the $ names are jq's, not the shell's
findings=$(jq -c '
  def badge: if type == "string" then {"must-fix": "🔴 **Must fix**", "should-fix": "🟡 **Should fix**", "nitpick": "⚪ **Nitpick**"}[.] else null end;
  def rank: {"must-fix": 0, "should-fix": 1, "nitpick": 2}[.];
  def language: (split(".") | if length > 1 then last else "" end) as $ext
    | {kt: "kotlin", kts: "kotlin", swift: "swift", java: "java", py: "python", sh: "bash", yml: "yaml",
       yaml: "yaml", json: "json", xml: "xml", md: "markdown", toml: "toml", gradle: "groovy",
       js: "javascript", ts: "typescript", sql: "sql", html: "html", css: "css"}[$ext] // "";
  # A fence longer than any run of backticks in the code, so the code can hold a fence of its own.
  def fence: "`" * ([3, ([scan("`+") | length] | max // 0) + 1] | max);
  [.[] | select(type == "object") | {
    path: ((.path // .file)
      | if type == "string" then (if env.WORKSPACE != "" then ltrimstr(env.WORKSPACE + "/") else . end) | ltrimstr("./") else null end
      | if . == "" then null else . end),
    line: ((.line // .end_line) | tonumber? // null),
    start_line: ((.start_line // null) | tonumber? // null),
    severity: (.severity | if type == "string" then ascii_downcase | gsub("^\\s+|\\s+$"; "") | gsub("[\\s_]+"; "-") else null end
      | if badge then . else "should-fix" end),
    summary, failure_scenario, category, fix, given_body: .body,
    suggestion: (.suggestion | if type == "string" then rtrimstr("\n") else null end),
    fixed: (env.FIXED_IN != "" and .outcome == "fixed")
  }
  | if .start_line != null and (.line == null or .start_line > .line) then .start_line = null | .suggestion = null
    elif .start_line == .line then .start_line = null
    else . end
  | if .fixed then .suggestion = null else . end]
  | sort_by(.severity | rank)
  | map(. as $f
    | ($f.given_body // (
        ([($f.severity | badge), ($f.category | select(.) | "_\(tostring)_")] | join(" · ")) + "\n\n"
        + "**" + ($f.summary // "Finding" | tostring) + "**"
        + (if $f.failure_scenario then "\n\n" + ($f.failure_scenario | tostring) else "" end)
        + (if $f.fix then "\n\n**How to fix:** " + ($f.fix | tostring) else "" end)) | tostring) as $text
    | (if $f.given_body != null then null else $f.suggestion end) as $code
    | ($code // "" | fence) as $fence
    | (if $f.start_line != null then "lines \($f.start_line)–\($f.line)" else "line \($f.line)" end) as $lines
    | {path, line, start_line, severity, fixed, short: $text,
       body: ($text + if $code == null then ""
         else "\n\n\($fence)suggestion\n" + (if $code == "" then "" else $code + "\n" end) + $fence end),
       loose: ($text + if $code == null or $f.line == null then ""
         elif $code == "" then "\n\nSuggested change: delete \($lines)."
         else "\n\nSuggested change for \($lines):\n\n\($fence)\($f.path // "" | language)\n\($code)\n\($fence)" end)})
  ' <<< "$input")
total=$(jq 'length' <<< "$findings")

if [ "$total" -eq 0 ]; then
  jq -n '{event: "COMMENT", body: "### Code review\n\nNo findings."}' | post
  exit 0
fi

as_list() {
  jq -r 'to_entries | map("\(.key + 1). " + (if .value.path then "`\(.value.path)" + (if .value.line then ":\(.value.line)" else "" end) + "`" else "(no file)" end)
    + "\n\n\(.value.body)") | join("\n\n")'
}

# Prints the header $1 followed by the findings of $2 as a list, in at most $3 characters: past that,
# the findings lose their suggestions, then the last ones are left out whole, so the cut never lands
# inside a code block. GitHub refuses a review body over 65,536 characters.
max_body=65000
with_list() {
  local text short n kept
  text="$1$(as_list <<< "$2")"
  if [ "${#text}" -gt "$3" ]; then
    short=$(jq -c 'map(.body = .short)' <<< "$2")
    n=$(jq 'length' <<< "$short")
    kept=$n
    text="$1$(as_list <<< "$short")"
    while [ "${#text}" -gt "$(($3 - 200))" ] && [ "$kept" -gt 0 ]; do
      kept=$((kept - 1))
      text="$1$(jq -c --argjson kept "$kept" '.[:$kept]' <<< "$short" | as_list)"
    done
    if [ "$kept" -lt "$n" ]; then
      text="$text

_$((n - kept)) more finding(s) left out: GitHub refuses a longer review body${run_url:+; see [the run]($run_url)}._"
    fi
  fi
  printf '%s' "$text"
}

# How many findings of each severity, after the count: "3 finding(s) (🔴 1 must fix · ⚪ 2 nitpicks)".
counts=$(jq -r '[group_by(.severity)[] | {key: .[0].severity, value: length}] | from_entries
  | [(."must-fix" // empty | "🔴 \(.) must fix"), (."should-fix" // empty | "🟡 \(.) should fix"),
     (.nitpick // empty | "⚪ \(.) nitpick" + (if . > 1 then "s" else "" end))]
  | if length > 0 then " (" + join(" · ") + ")" else "" end' <<< "$findings")

fixed=$(jq -c 'map(select(.fixed))' <<< "$findings")
fixed_count=$(jq 'length' <<< "$fixed")
findings=$(jq -c 'map(select(.fixed | not))' <<< "$findings")
left=$(jq 'length' <<< "$findings")
lead="$total finding(s)$counts"
fixed_section=""
if [ "$fixed_count" -gt 0 ]; then
  if [ "$left" -eq 0 ]; then
    body=$(with_list "### Code review

$lead, all fixed in $FIXED_IN:

" "$fixed" "$max_body")
    jq -n --arg body "$body" '{event: "COMMENT", body: $body}' | post
    exit 0
  fi
  lead="$lead: $fixed_count fixed in $FIXED_IN, listed at the end, and $left left"
  # Half the body at most, so the findings left for the author always fit.
  fixed_section=$(with_list "

Fixed in $FIXED_IN:

" "$fixed" "$((max_body / 2))")
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
# on another line of a changed file is anchored to the nearest hunk's edge, naming its own line. Only
# a comment on the very lines the finding names keeps its suggestion block: GitHub would apply one
# anywhere else to the wrong lines.
split=$(jq -c --argjson hunks "$hunks" '
  def hunk($path; $line): first($hunks[] | select(.path == $path and .from <= $line and $line <= .to)) // null;
  def nearest($path; $line): [$hunks[] | select(.path == $path)]
    | if length == 0 then null else min_by(if $line < .from then .from - $line else $line - .to end) end;
  map(. as $f
    | if $f.line == null or $f.path == null then . + {anchored: false}
      else hunk($f.path; $f.line) as $h
      | if $h != null then
          (if .start_line != null and .start_line < $h.from then .start_line = null | .body = .loose else . end)
          + {anchored: true}
        else nearest($f.path; $f.line) as $n
        | if $n == null then . + {anchored: false}
          else {path, line: (if $f.line < $n.from then $n.from else $n.to end), start_line: null,
                body: ("_On line \($f.line), outside the diff; anchored to the nearest changed line._\n\n" + .loose),
                anchored: true} end
        end
      end)
  | {inline: map(select(.anchored)), body: map(select(.anchored | not) | .body = .loose)}' <<< "$findings")
inline=$(jq -c '.inline' <<< "$split")
outside=$(jq -c '.body' <<< "$split")
inline_count=$(jq 'length' <<< "$inline")

if [ "$inline_count" -gt 0 ]; then
  summary="### Code review

$lead"
  if [ "$inline_count" -eq "$left" ]; then
    summary="$summary, as inline comments."
  else
    summary=$(with_list "$summary: $inline_count as inline comments, the others below, on lines GitHub can't anchor a comment to. They open no thread, so they don't hold the merge.

" "$outside" "$((max_body - ${#fixed_section}))")
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

loose=$(jq -c 'map(.body = .loose)' <<< "$findings")
body=$(with_list "### Code review

$lead, on lines GitHub can't anchor a comment to. They open no thread, so they don't hold the merge.

" "$loose" "$((max_body - ${#fixed_section}))")$fixed_section
jq -n --arg body "$body" '{event: "COMMENT", body: $body}' | post
