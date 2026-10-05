---
name: project-web-wasm-spike
description: Oct/2026 spike (PR #514, merged) proved the whole app runs as CMP wasmJs in the browser; lists the real blockers and traps found
metadata:
  node_type: memory
  type: project
  originSessionId: ddd95c1f-29d9-43ff-bf71-a3dda3c16f96
  modified: 2026-10-03T03:51:03.471Z
---

Spike (2026-10-03, branch `chore/web-wasm-spike`, worktree `.claude/worktrees/web-wasm-spike`, shipped as PR #514, squash-merged via merge-when-green on 2026-10-03; brings DataStore 1.3.0-alpha11 to every platform): `wasmJs { browser() }` in the KMP convention + a `:webApp` module (ComposeViewport) runs the full app in Chrome — Room on OPFS via `sqlite-web` WebWorkerSQLiteDriver + a vendored worker (core/provider/room/web-worker), Bible download from Storage, persistence across reloads.

Findings worth remembering:
- DataStore web exists only in 1.3.0-alpha (`WebLocalStorage` in datastore-core-okio); catalog switched to the `-core` artifacts.
- Room web needs suspend migrations/`execSQL`, KSP `kspWasmJs`, COOP/COEP headers (OPFS) — COOP same-origin may break OAuth popups.
- Koin on wasm keys types by simple class name → same-named classes in different packages collide (ClassCastException). Fixed the Convert*UseCase dupes by moving them to core/date; a uniqueness lint would prevent it.
- `MutablePreferences.remove(key)` returns null-as-T → crashes on wasm, use `-=`.
- Desktop billing (RevenueCat REST), remote config and ui/component actuals moved to a `nonMobileMain` (jvm+wasm) source set; manual dependsOn disables the default hierarchy, so iosMain must be wired by hand.
- Backend: edge-function CORS (`_shared/auth-middleware.ts`) must allow `x-region` (supabase-kt sends it on every function call).
- SQLite worker must use the opfs-sahpool VFS (exclusive per tab, falls back to OpfsDb in a second tab = separate DB): the default OpfsDb VFS made reloads 6–12 s and the sync pull trickle in (looked like missing progress); SAH pool: reload 2–4 s, 196 chapter reads applied in ~130 ms. Changing worker.js needs a package version bump + `kotlinWasmUpgradeYarnLock`.
- Snapshot pull runs 11 synchronizers sequentially (~0.3–0.5 s fetch each, ~4.5 s total); ProfileSync is last, so the avatar arrives last — parallelizing pullAll is the fix (mobile benefits too).
- Prod bundle ≈ 5.4 MB brotli (app 2.4 + skiko 2.6 + sqlite 0.34); <3 s to content on localhost. No browser history (CMP-8924). `isA11YEnabled = true` builds an ARIA mirror in a shadow root.
- Web stubs: analytics/crash no-op (MP api_secret must not ship to browsers), avatar cropper unsupported, NetworkTime falls back to device clock (Date header not CORS-exposed).

Related: [[project-desktop-analytics-measurement-protocol]], [[project-navigation3-migration]], [[project-feature-isolation]].
