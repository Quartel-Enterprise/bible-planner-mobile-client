# sync_failed

**Tier:** P2 | **Domain:** Auth

Captures a failed snapshot pull: a logged-in, connected user did not receive (part of) their remote data. This is the sync pipeline's error rate; spikes point at backend or schema regressions that would otherwise surface only as vague "my progress disappeared" support tickets.

## When it fires

A `Synchronizer.pullSnapshot()` throws during the `SyncCoordinator`'s pull-on-connect cycle.

## Trigger source

`core/sync/.../SyncCoordinator.kt` — `pullOnConnected()`, the `onFailure` branch of `suspendRunCatching { synchronizer.pullSnapshot() }` (not a ViewModel; background pipeline)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Failures are caught per synchronizer, so one connected-cycle can fail for one dataset while others succeed; a cycle with any failure counts as `sync_failed` (see [sync_completed](sync_completed.md)). If dataset-level detail proves necessary, add a param during implementation rather than new events.
- The failure is already reported to Crashlytics as a non-fatal via `Logger.e`; this event exists for rate monitoring in GA4, not for stack traces.
- Silent to the user — there is no error UI on this path, so analytics is the only product-side visibility.
