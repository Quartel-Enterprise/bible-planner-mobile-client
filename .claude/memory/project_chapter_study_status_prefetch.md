---
name: project-chapter-study-status-prefetch
description: "PR #562 (Oct/26) — reader prefetches the chapter study quota status per visible chapter; in-memory store trusted only for \"open\", cleared on generation end"
metadata:
  node_type: memory
  type: project
  originSessionId: 9d3f16b5-80f4-4bba-8be7-00e57261ac86
  modified: 2026-10-04T16:41:09.496Z
---

PR #562 (merged 2026-10-04) removed the spinner on the reader's "Study" pill/card: the reader prefetches `get-chapter-study-status` whenever the visible chapter changes (narrow layout only, `collectLatest`), into the in-memory `ChapterStudyStatusPrefetchStore` keyed by user + version + chapter + language.

**Why:** the tap used to wait on that edge function for every signed-in free user without a cached study. Caching was deliberately memory-only and asymmetric: a stored status is trusted only when it opens the study (a stale "open" self-corrects via the study screen + server limit check); "limit reached" is always re-fetched so nobody with free studies sees the paywall. The store is cleared when a generation completes or the server refuses one for the limit, and a fetch in flight across a clear() is not stored (clearCount check).

**How to apply:** any new caller deciding chapter study access should go through `ChapterStudyQuotaChecker` (shared quota rule + trust rule) instead of calling `fetchStatus` directly; don't persist this status to disk. Same "head start, never the truth" idea as the day study's `DayStudyQuotaPrefetchStore`.
