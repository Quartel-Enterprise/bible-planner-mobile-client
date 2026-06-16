# Commit timestamps and the "next day" GitHub grouping

If you browse the commit list on GitHub and see commits grouped under a date **ahead** of
your local one — e.g. an evening commit from June 15 (BRT) listed under "Commits on Jun 16" —
nothing is wrong with the history. It is a display artifact of how GitHub groups the list. This
doc explains the real rule and why there is no cosmetic fix.

## The rule: GitHub groups by the UTC date

The "Commits on <date>" headers on GitHub's commit list group commits by the **UTC calendar date
of the author date** — i.e. by the commit's absolute instant, normalized to UTC. It does **not**
use the timezone offset stored in the commit, and it does **not** use the viewer's timezone.

You can confirm this from the page's own embedded data (`react-app.embeddedData` →
`payload.commitGroups`): a commit authored at `2026-06-15T21:51-03:00` carries the group title
`Jun 16, 2026`, because that instant is `2026-06-16T00:51Z` in UTC.

The boundary is therefore **midnight UTC**. In Brazil (GMT-3, no DST) that is **21:00 local**:

| Author instant (BRT) | Same instant in UTC | GitHub group |
|---|---|---|
| Jun 15, 20:46 | Jun 15, 23:46 | **Jun 15** |
| Jun 15, 21:07 | Jun 16, 00:07 | **Jun 16** |
| Jun 15, 21:51 | Jun 16, 00:51 | **Jun 16** |

So any commit authored after ~21:00 BRT shows up under the next day on GitHub. This repo's PRs are
usually squash-merged in the evening Brazil time, which is why several land past the UTC boundary.

## Why you cannot "fix" the remote grouping

Because the grouping is keyed on the **absolute instant** (UTC), the stored timezone offset is
irrelevant to it. Re-stamping commits with a `-0300` offset (e.g. via `git filter-branch`) keeps
the exact same instant, so GitHub groups them identically — the rewrite changes the SHAs but not
the day headers. The only way to move a commit to the previous day on GitHub would be to change
its **actual timestamp** to before midnight UTC, i.e. to falsify when the work happened. Don't do
that for a cosmetic list header.

## The history itself is fine

The absolute timestamps (`%ct`) stay monotonic and correctly ordered. Rendered in your own
timezone, every commit lands on the correct local day:

```bash
git log --date=local --pretty=format:'%h  %ad  %s'
```

## Local configuration

This repo relies on git's `log.date=local` setting so the CLI always renders commit dates in the
**current machine's timezone**:

```bash
git config --global log.date local
```

This is travel-proof: it follows whatever timezone your OS is set to, so it needs no maintenance
when you work from another country. Do **not** hardcode `TZ` in your shell profile — git already
reads the OS timezone for new local commits, and pinning it would make your commits report the
wrong zone while traveling.

Note this only affects your **local** `git log`. GitHub's web grouping is computed in UTC on the
server and has no viewer-side timezone setting, so evening-BRT commits will still appear a day
ahead there. That is purely cosmetic and is left as-is.
