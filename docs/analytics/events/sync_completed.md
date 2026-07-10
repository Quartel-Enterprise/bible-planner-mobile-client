# sync_completed

**Tier:** P2 | **Domain:** Auth

Captures a successful snapshot pull of the user's remote data. Together with [sync_failed](sync_failed.md) it gives the health rate of the sync pipeline — the feature the whole auth funnel exists to unlock — and confirms that logged-in users actually receive their data after login and reconnections.

## When it fires

The `SyncCoordinator` pulls the full remote snapshot successfully — on every realtime `CONNECTED` transition for an authenticated user (right after login, on cold start, and on each reconnection).

## Trigger source

`core/sync/.../SyncCoordinator.kt` — `pullOnConnected()`, when `pullSnapshot()` succeeds for every registered `Synchronizer` (not a ViewModel; this is a session-scoped background pipeline driven by `ObserveSync`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- The pull loops over all registered `Synchronizer`s (each dataset pulls its own snapshot); log one event per connected-cycle in which **all** pulls succeed, not one per synchronizer — mixed outcomes count as [sync_failed](sync_failed.md).
- Fires on every reconnection, so expect multiple events per session; analyze as a rate against `sync_failed`, not as a user-level conversion.
- Related: [login](login.md) (the login that starts the pipeline), [logout_failed](logout_failed.md) (push-side flush failures at logout).
