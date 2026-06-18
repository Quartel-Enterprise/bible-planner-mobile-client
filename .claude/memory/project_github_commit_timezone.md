---
name: project-github-commit-timezone
description: GitHub groups the commit-list by a stale account timezone (+0200); mitigation + open bug report being monitored.
metadata: 
  node_type: memory
  type: project
  originSessionId: 8f74446f-ef71-4046-90c7-de597a695fe1
---

GitHub's commit-list page ("Commits on <date>" headers) groups commits by the **account's server-side timezone**, NOT the commit's stored offset, NOT the `tz` cookie, NOT the browser. This account timezone got stuck at **+0200 (CEST)** from past Claude Code cloud-sandbox activity, so commits made in the evening BRT show under the next day on github.com (the per-commit timestamp renders correctly in -0300; only the gray day header is off). Verified empirically: rewriting commit author offsets to -0300 did NOT move the headers.

**Mitigation applied (2026-06-16):** set `TZ=America/Sao_Paulo` in the **Claude Code cloud environment** config at claude.ai/code → environment "Default" → Environment variables. This is per-account, NOT committed to the repo (committing a fixed TZ would impose it on collaborators and doesn't even reach cloud sandboxes — only `hooks` from committed `.claude/settings.json` carry over to cloud). Local git also has `log.date=local` set globally.

**Bug reported:** GitHub Community discussion #199224 — https://github.com/orgs/community/discussions/199224 (category Repositories). Monitored daily by scheduled task `monitor-github-discussion-199224` (9:05 AM BRT).

Do NOT rewrite history / force-push to fix this — it's confirmed ineffective (the grouping ignores the commit offset). A doc that tried to explain this lived at `docs/commit-timezones.md` but was removed in PR #171 because it kept stating the rule wrong.
