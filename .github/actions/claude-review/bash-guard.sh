#!/usr/bin/env bash
# PreToolUse hook of the review session (see action.yml): lets a Bash command run only when it is one
# of the few the review needs, written so plainly that nothing in it can print the runner's
# environment, where the GitHub token lives (the Claude OAuth token is scrubbed from it). It runs
# before every Bash call, including the read-only commands Claude Code approves on its own.
#
# A command passes when it is a single line of letters, digits, spaces and _ . , : / = @ + -, so no
# quoting, expansion ($VAR, $(...), `...`), escaping, redirection, pipe or second command can hide
# anything; when it starts with gh pr view/diff/list, gh issue view/list, gh search, git status,
# git merge-base or is git branch --show-current; and when no word is gh's --jq or --template, or a
# bundle of short flags holding -q or -t, which read the environment through gojq and Go templates.
# Anything else, a command the guard can't read included, is refused: exit 2 tells the model why.
set -uo pipefail

refuse() {
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
case "${words[0]:-} ${words[1]:-} ${words[2]:-}" in
  "gh pr view"* | "gh pr diff"* | "gh pr list"* | "gh issue view"* | "gh issue list"* | "gh search "*) ;;
  "git status"* | "git merge-base"*) ;;
  "git branch --show-current")
    [ "${#words[@]}" -eq 3 ] || refuse "git branch only runs as git branch --show-current"
    ;;
  *) refuse "only gh pr view/diff/list, gh issue view/list, gh search, git status, git merge-base and git branch --show-current run here" ;;
esac

for word in "${words[@]}"; do
  case "$word" in
    --jq | --jq=* | --template | --template=*) refuse "gh's --jq and --template can read the environment" ;;
  esac
  if [[ "$word" =~ ^-[A-Za-z]+$ && "$word" == *[qt]* ]]; then
    refuse "gh's -q and -t can read the environment"
  fi
done
exit 0
