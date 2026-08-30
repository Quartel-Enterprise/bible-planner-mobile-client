---
name: project-realtime-foreground-gate
description: "Supabase realtime channels are foreground-only (PR #386); background setAuth crashes (websocket/TokenExpired) were unfixable app-side and Aug/2026 Crashlytics triage outcomes."
metadata: 
  node_type: memory
  type: project
  originSessionId: eb9774aa-6e4b-4a34-ae1a-197f808e739f
  modified: 2026-08-30T18:47:00.621Z
---

PR #386 (2.8.0) gates every `Synchronizer.observeRealtime` behind `AppForegroundStateHolder` (core/model, fed by ON_START/ON_STOP in `App.kt`, collected in `SyncCoordinator`). Reason: supabase-kt (still in 3.8.0) launches `setAuth` in its own unsupervised scope on session refresh whenever `Realtime.status == CONNECTED`; anything thrown there (`websocket` getter → "Websocket not yet initialized", `checkAccessToken` → TokenExpiredException r40) crashes the app and cannot be caught app-side. With no subscribed channels the socket disconnects (`disconnectOnNoSubscriptions`), so the listener never runs in background; missed changes are covered by the snapshot pull on the reconnect CONNECTED transition. Related: [[project-spontaneous-logout-2-0-0]].

Crashlytics triage (Aug 2026, all open fatal Android issues resolved): NoSuchMethodError issues 7c080f7c (WorkManager `JobScheduler34.forNamespace`) and 31fdcf9b (`WindowInsets.Type.systemOverlays`) are MUTED — devices reporting SDK 34 with pre-release/modified Android 14 frameworks missing API-34 methods (same installation UUID in both samples); androidx calls them unguarded even at androidx-main, so no app-side fix or bump helps. OOM e1fb5ab6 was fixed by #372/2.7.1 (relation stops at VerseEntity). Duplicate-key f26e8107's 2.4.0 regression was navigation3's own SaveableStateHolder key-reuse bug, fixed upstream in navigation3-runtime 1.1.6 (b/516312097), shipped in 2.5.0.
