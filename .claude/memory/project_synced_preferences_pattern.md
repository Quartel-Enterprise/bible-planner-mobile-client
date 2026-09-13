---
name: synced-preferences-pattern
description: Adding a synced preference needs zero backend/sync-engine changes — the pipeline is key-agnostic; mirror the theme/language repository pattern.
metadata: 
  node_type: memory
  type: project
  originSessionId: 05a476d7-f8ad-429a-86df-e4909444ec6f
  modified: 2026-08-31T03:10:27.682Z
---

The cross-device preference sync pipeline (Supabase `user_preferences` key/value table + `SyncedPreferenceDao`/`OfflineFirstSynchronizer`/realtime channel) is fully key-agnostic: LWW trigger, RLS, realtime publication, no-op guard and account-deletion cascade already cover any new key.

**How to apply:** to sync a new preference, only add keys to `SyncedPreferenceKeys`, mirror the `ThemeSelectionRepositoryImpl` shape (DataStore = render source; `mirrorIfSyncEnabled` on writes; `setSyncEnabled(true)` snapshots current values; `applySynced*` = DataStore-only to avoid echo), an app-scoped `Observe*Sync` collector launched from `InitializeAppContentUseCase`, the login-gated `SyncOption`/`SyncSwitch` composable pair (duplicated per sheet, not shared), and a `LoginWarningReason.Preferences.*` case (its `entries` list must be extended). Done for study suggestion in PR #403.
