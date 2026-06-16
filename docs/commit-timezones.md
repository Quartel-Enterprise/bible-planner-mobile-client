# Commit timestamps and the "next day" GitHub grouping

If you browse the commit list on GitHub and see commits grouped under a date **ahead** of
your local one — e.g. an evening commit from June 15 (BRT) listed under "Commits on Jun 16" —
nothing is wrong with the history. It is a timezone *display* artifact. This doc explains why
and how the repo is configured to avoid the confusion while debugging.

## Why it happens

Every git commit stores two things for each of its author/committer dates:

1. an **absolute instant** (a Unix timestamp), and
2. a **timezone offset** used only for display (e.g. `-0300`, `+0200`).

GitHub's commit-list page groups commits by the date **rendered in each commit's own stored
offset** — it does not normalize to the viewer's timezone.

PRs in this repo are produced and squash-merged through the Claude Code cloud environment, whose
sandbox runs in **UTC+2 (CEST)**. So the author date baked into those commits carries a `+0200`
offset — not the maintainer's `-0300` (GMT-3, Brazil). Any merge done in the evening Brazil time
crosses midnight in `+0200`:

| Offset used to render | Shown as | GitHub groups under |
|---|---|---|
| `-0300` (your machine) | Jun 15, 21:07 | **Jun 15** ✓ |
| `+0200` (cloud sandbox) | Jun 16, 02:07 | **Jun 16** ✗ |

Same absolute instant — only the label differs by ~5h.

How to tell the two sources apart in `git log`:

| Source | Stored offset |
|---|---|
| Commits made locally on the maintainer's machine | `-0300` (follows the OS timezone) |
| GitHub Actions runners | `+0000` (UTC) |
| Commits/merges via the Claude Code cloud agent | `+0200` (CEST) |

## The history itself is fine

The absolute timestamps (`%ct`) stay monotonic and ordered regardless of the displayed offset.
Rendered in your own timezone, every commit lands on the correct local day:

```bash
git log --date=local --pretty=format:'%h  %ad  %s'
```

## Repo configuration

This repo relies on git's `log.date=local` setting so the CLI always renders commit dates in the
**current machine's timezone** instead of each commit's stored offset:

```bash
git config --global log.date local
```

This is travel-proof: it follows whatever timezone your OS is set to, so it needs no maintenance
when you work from another country. Do **not** hardcode `TZ` in your shell profile — git already
reads the OS timezone for new local commits, and pinning it would make your commits report the
wrong zone while traveling.

The GitHub *web* commit list still groups by each commit's stored offset (there is no viewer-side
setting for it), so cloud-agent commits may keep appearing a day ahead there. That is cosmetic and
intentionally left as-is rather than rewriting history on `main`.
