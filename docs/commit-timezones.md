# Commit timestamps and the "next day" GitHub grouping

If you browse the commit list on GitHub and see a commit grouped under a date **ahead** of the one
you expect — e.g. an evening commit listed under "Commits on Jun 17" when you made it on Jun 16 —
nothing is wrong with the history. It is a display artifact driven by the timezone **offset stored
in the commit**. This doc explains the rule and how this repo avoids the confusion.

## The rule: GitHub groups by the commit's stored offset

The "Commits on <date>" headers on GitHub's commit list group commits by the **author date rendered
in the timezone offset stored in that commit** — not by UTC, and not by the viewer's timezone.

You can confirm this empirically: a commit whose author date is `2026-06-17T00:48:49+02:00` lands
under "Jun 17" — even though that same instant is `Jun 16` in UTC (`22:48Z`) and `Jun 16` in your
local `-0300` (`19:48`). The only date that matches the GitHub header is the one in the commit's
**own stored offset** (`+0200` → Jun 17).

> Watch out: the GitHub REST API (`/commits`) returns dates normalized to **UTC** (`...Z`), and a
> page payload fetched without a `Time-Zone` header may also show UTC-grouped titles. Those do **not**
> reflect what the web UI actually renders. Trust the rendered page, which groups by the stored offset.

## Where the wrong offset comes from

Commits stamped with an unexpected offset in this repo come from the **Claude Code cloud sandbox**,
which runs in a European timezone (`+0200`, CEST). A commit authored there in the evening Brazil time
gets a `+0200` label, pushing its local date — and therefore its GitHub group — to the next day.

| Source | Stored offset |
|---|---|
| Commits made locally on your machine | follows your OS timezone (`-0300` in Brazil) |
| GitHub Actions runners | `+0000` (UTC) |
| Commits authored in the Claude Code cloud sandbox | `+0200` (CEST) |

## The fix: set the timezone of the cloud sandbox

Set `TZ=America/Sao_Paulo` in the **cloud environment configuration** at claude.ai/code
(environment → *Environment variables*, `.env` format). This makes commits authored by the cloud
agent carry `-0300`, so they group under the correct day.

Do **not** commit a fixed `TZ` into the repo (e.g. in `.claude/settings.json` or a `SessionStart`
hook): it would impose your timezone on every collaborator and is not travel-proof. The cloud
environment setting is per-account and is not part of the repository — and note that user-level or
local-only config (`~/.claude/settings.json`, `.claude/settings.local.json`) does **not** reach
cloud sessions, which only see what is committed.

## The history itself is fine

The absolute timestamps (`%ct`) are always monotonic and correctly ordered, whatever the offset.
Rendered in your own timezone, every commit lands on the correct local day:

```bash
git log --date=local --pretty=format:'%h  %ad  %s'
```

This repo sets `log.date=local` globally so the CLI always renders dates in the current machine's
timezone — travel-proof, no maintenance:

```bash
git config --global log.date local
```

This only affects your **local** `git log`. The GitHub web grouping is computed from each commit's
stored offset and has no viewer-side setting, so commits already authored with `+0200` keep showing a
day ahead there until they are re-stamped.
