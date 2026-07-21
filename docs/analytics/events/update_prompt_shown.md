# update_prompt_shown

**Tier:** P1 | **Domain:** Updates

Captures the "Atualização disponível" sheet being shown to the user because a newer app version is available. It is the entry step of the in-app update funnel and, via `source`, separates the passive startup auto-prompt from an explicit manual check.

## When it fires

The `InAppUpdateNavRoute` sheet enters composition — either fired automatically on app launch when an update is detected (once per session), or after the user manually taps "Buscar atualizações" on the Profile screen and an update is found.

## Trigger source

`feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/InAppUpdateViewModel.kt` — `init` block.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `startup` | Where the prompt came from: `startup` (background check on app launch) \| `manual` (user tapped "Buscar atualizações") |
| `version` | string | `2.0.0` | Available version. Present on iOS (from the App Store lookup); **omitted on Android**, where Google Play exposes only a version code, not a name |

## Notes

- A manual check that finds **no** update produces only [profile_option_clicked](profile_option_clicked.md) with `option=check_for_update` and no `update_prompt_shown` — so "manual checks that found an update" = manual `update_prompt_shown`, and "manual checks with no update" = the difference.
- The sheet impression is also captured generically by [destination_view](destination_view.md) with `destination_name=in_app_update`; this event adds the `source`/`version` funnel context that `destination_view` deliberately omits.
- The startup prompt is throttled to once per app session (in-memory `UpdatePromptSessionGuard`), so at most one `startup` impression is expected per launch.
- Desktop never fires this event (the checker is a no-op and the row is hidden).
