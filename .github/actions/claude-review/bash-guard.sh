#!/usr/bin/env bash
# PreToolUse hook of the review session (see action.yml): lets a Bash command run only when it is one
# of the few the review needs, written so plainly that nothing in it can print the runner's
# environment, where the GitHub token lives (the Claude OAuth token is scrubbed from it). It runs
# before every Bash call, including the read-only commands Claude Code approves on its own. Usage:
# bash-guard.sh <state dir>, with the hook's input on stdin.
#
# A command passes when it is a single line of letters, digits, spaces and _ . , : / = @ + -, so no
# quoting, expansion ($VAR, $(...), `...`), escaping, redirection, pipe or second command can hide
# anything; when its first words are exactly gh pr view/diff/list, gh issue view/list, gh search,
# git status or git branch --show-current; and when no word is gh's --jq or --template, or a word
# of short flags holding q or t, even with a value attached (-q=..., -wq...), which read the
# environment through gojq and Go templates. Anything else, a command the guard can't read
# included, is refused: exit 2 tells the model why, and the refusal goes to <state dir>/.guard-log
# with only the command's first words that look like a program or a subcommand, for the run's
# warnings.
set -uo pipefail

log=${1:+$1/.guard-log}
command=""

refuse() {
  if [ -n "$log" ]; then
    local -a w=()
    local shown="" i
    read -r -a w <<< "$command" || true
    for i in 0 1 2; do
      [[ "${w[i]:-}" =~ ^[A-Za-z][A-Za-z0-9._-]*$ ]] || break
      shown+="${shown:+ }${w[i]}"
      [ "$i" -eq 1 ] && [ "${w[0]}" != gh ] && break
    done
    echo "Bash(${shown:-?})" >> "$log" 2> /dev/null || true
  fi
  echo "Refused by the review's guard: $1. Read files with the Read tool, take the diff from gh pr diff, and search without quotes." >&2
  exit 2
}

command=$(jq -er '.tool_input.command | strings' 2> /dev/null) || refuse "the guard couldn't read the command"

case "$command" in
  *$'\n'* | *$'\r'*) refuse "the command spans several lines" ;;
esac
if ! [[ "$command" =~ ^[A-Za-z0-9\ _.,:/=@+-]+$ ]]; then
  refuse "only letters, digits, spaces and _ . , : / = @ + - are allowed"
fi

read -r -a words <<< "$command"
case "${words[0]:-}/${words[1]:-}/${words[2]:-}" in
  gh/pr/view | gh/pr/diff | gh/pr/list | gh/issue/view | gh/issue/list | gh/search/?*) ;;
  git/status/*) ;;
  git/branch/--show-current)
    [ "${#words[@]}" -eq 3 ] || refuse "git branch only runs as git branch --show-current"
    ;;
  *) refuse "only gh pr view/diff/list, gh issue view/list, gh search, git status and git branch --show-current run here" ;;
esac

for word in "${words[@]}"; do
  case "$word" in
    --jq | --jq=* | --template | --template=*) refuse "gh's --jq and --template can read the environment" ;;
    --*) ;;
    -*[qt]*) refuse "gh's -q and -t can read the environment, even with a value attached" ;;
  esac
done
exit 0
