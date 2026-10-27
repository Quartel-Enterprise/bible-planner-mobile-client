#!/usr/bin/env bash
# PreToolUse hook of the review session (see action.yml): refuses a Bash command that could print the
# runner's environment, where the Claude OAuth token and the GitHub tokens live. The allowlist only
# matches the start of a command, so an allowed gh command still takes --jq, -q, --template or -t
# (gojq's $ENV and env, Go templates' env), and any command expands $VAR, ${VAR:...}, $(...) and
# `...` before it runs, the value then showing up in its output or in gh's error. Redirections are
# refused too, since no allowed command needs one. Exit 2 refuses the call and tells the model why;
# anything else lets the allowlist decide.
set -uo pipefail

command=$(jq -r '.tool_input.command // ""' 2> /dev/null) || exit 0

refuse() {
  echo "Refused by the review's guard: $1. Read files with the Read tool and take the diff from gh pr diff." >&2
  exit 2
}

case "$command" in
  *'$'* | *'`'*) refuse "the command expands a variable or a command substitution" ;;
  *'<'* | *'>'*) refuse "the command redirects input or output" ;;
esac

# Word by word, a quoted word included: --jq, --jq=..., --template, -q, -t, and short flags bundled
# with q or t (-qL).
read -r -a words <<< "$command"
for word in "${words[@]}"; do
  word=${word#[\"\']}
  case "$word" in
    --jq | --jq=* | --template | --template=*) refuse "gh's --jq and --template can read the environment" ;;
    --*) ;;
    -*[qt]*) refuse "gh's -q and -t can read the environment" ;;
  esac
done
exit 0
