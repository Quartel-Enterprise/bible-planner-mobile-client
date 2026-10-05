---
name: project_ai_code_review
description: "#519 AI review — create-pr runs /code-review high + review-conventions before the PR; claude-review.yml automatic on PR open (Max OAuth token; was ai-review label until #523); docs links in ai_agents.md are NOT auto-loaded."
metadata:
  node_type: memory
  type: project
  originSessionId: 4be91b77-a0fc-4703-b041-033cc9d314c4
  modified: 2026-10-03T16:07:02.235Z
---

PR #519 (issue #515, merged 2026-10-03) added two review layers:

- **Local (main layer):** `create-pr` step 8 writes the PR description first (it is the reviewers' only source of intent), step 9 runs `/code-review high` + the forked `review-conventions` skill (docs read per touched area + product invariants list that replaced the proposed `docs/business-rules.md`). Skipped for md/release-notes/version/catalog/store-metadata-only branches or on request. `/code-review` diffs `@{upstream}...HEAD` + `git diff HEAD`, so untracked files need `git add -N .` and a pushed branch needs the branch name as target.
- **GitHub (automatic since #523, merged 2026-10-03; before that the `ai-review` label):** `.github/workflows/claude-review.yml` runs the official `code-review@claude-code-plugins` plugin with `--comment` on pull_request opened/reopened/ready_for_review (drafts skipped), auth `CLAUDE_CODE_OAUTH_TOKEN` (set 2026-10-03, 1-year token → expires ~2027-10-03). Advisory only (findings never fail the check), but merge-when-green waits for it on the opening head commit and a failed run (expired token) holds the merge until rerun; later pushes aren't reviewed on their own (plugin skips already-commented PRs).
- **GitHub (on request):** `claude-review-on-request.yml` on the `ai-review` label re-reviews (prompt tells the plugin to ignore the already-commented skip and not repeat issues), skips if any claude-review/-on-request run of the branch has an unfinished job, then removes the label. Separate workflow + job name `review-on-request` so other labels' skipped checks never hide the opening `review` check. Shared step lives in composite `.github/actions/claude-review`.

**Gotchas learned:**
- The `[@docs/...](...)` entries in `docs/ai_agents.md` do NOT load the docs into context (only CLAUDE.md + ai_agents.md load); loading all would cost ~37k tokens per session.
- claude-code-action skips (green, "Workflow validation failed") whenever the PR's workflow file differs from the default branch's, so a workflow change can only be tested after merge. As of the merge, the first real run (token + whether the `claude` org app has this repo selected) was still untested.
- The action fails the step when `--max-turns` is hit, so no `--max-turns` is set.
- Silent green no-op (run 37138636041 on PR #522: 3 turns, ~$0.06, nothing posted): the plugin launches its agents in the background and the action breaks at the first SDK `result` (anthropics/claude-code-action#1646, #1499). Fix in the composite action: `CLAUDE_CODE_DISABLE_BACKGROUND_TASKS=1` + disallow ScheduleWakeup/Monitor/SendMessage. Never use `show_full_output` to debug: the repo is public; instead #544 (merged 2026-10-04) prints `::warning::` per denied tool from the execution file (tool name, Bash program+subcommand only). First real runs: #543 posted (81 s, $0.30, 1 denial of unknown tool, pre-#544), #544 posted with 0 denials. Verify after merge that the first automatic review actually posts.

Memory-only conventions are being turned into ktlint rules separately (branch chore/ktlint-memory-conventions). See [[feedback_ktlint_custom_enforcement]].
