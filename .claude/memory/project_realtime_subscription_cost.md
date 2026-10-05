---
name: project-realtime-subscription-cost
description: "Supabase Realtime subscription DB cost is per postgres_changes binding, not per channel; #559 (one channel per user) and a background grace period were both rejected Oct/2026."
metadata:
  node_type: memory
  type: project
  originSessionId: 9680ad8b-f996-432b-854b-402da394004c
  modified: 2026-10-04T16:03:54.891Z
---

Realtime's `Subscriptions.create` runs one `INSERT into realtime.subscription` per postgres_changes binding (inside one transaction per join), and `SubscriptionManager` already batches deletes (`delete_multi`, up to 1000 ids). So merging the 12 sync channels into one (#559) saves only ~11 commits per foreground. Total cost is small anyway: ~1 MB WAL/day, ~12 s DB/day (pg_stat_statements since 2026-01-02). The Disk IO warning comes from the Nano compute swapping.

A grace period before dropping channels on background was also rejected: it keeps the websocket open in background (the state [[project-realtime-foreground-gate]] removed), and iOS suspension freezes the timer, so channels would never drop.

**Why:** avoids re-proposing channel consolidation or a grace period as DB-IO fixes.
**How to apply:** to trim DB IO, look at the `chapter_reads` upserts first: ~953 MB WAL, 3.35M updates on 116k rows (the no-op skip trigger only landed 2026-08-13, so the current rate is unknown). Since 2.8.4 the "Websocket not yet initialized" crash also fires from `sendHeartbeat` (mostly in background).
