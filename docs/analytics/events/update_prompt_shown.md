# update_prompt_shown

**Tier:** P1 | **Domain:** Updates

Captures an update prompt being shown to the user because a newer app version is available. It is the entry step of the in-app update funnel and, via `source`, separates the passive auto-prompt from an explicit manual check.

## When it fires

An update was detected — either automatically when the app is opened or brought back to the foreground (at most once an hour), or after the user manually taps "Buscar atualizações" on the Profile screen. What is shown depends on the platform: Android launches the Google Play flexible update flow directly, while iOS presents the in-app "Atualização disponível" sheet (`InAppUpdateNavRoute`).

## Trigger source

- Android: `feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/domain/usecase/impl/ShowUpdatePromptUseCase.kt`, right before starting the Play update flow.
- iOS: `feature/in_app_update/src/commonMain/kotlin/com/quare/bibleplanner/feature/inappupdate/presentation/InAppUpdateViewModel.kt` — `init` block.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `startup` | Where the prompt came from: `startup` (background check on app launch or on return from background) \| `manual` (user tapped "Buscar atualizações") |
| `version` | string | `2.0.0` | Available version. Present on iOS (from the App Store lookup); **omitted on Android**, where Google Play exposes only a version code, not a name |

## Notes

- A manual check that finds **no** update produces only [profile_option_clicked](profile_option_clicked.md) with `option=check_for_update` and no `update_prompt_shown` — so "manual checks that found an update" = manual `update_prompt_shown`, and "manual checks with no update" = the difference.
- On iOS the sheet impression is also captured generically by [destination_view](destination_view.md) with `destination_name=in_app_update`; this event adds the `source`/`version` funnel context that `destination_view` deliberately omits. Android has no sheet, so it fires this event with no matching `destination_view`.
- The automatic prompt is throttled to at most one per hour. The last prompt timestamp is persisted in DataStore (`update_prompt_last_prompted_at`), so the cooldown survives the app being killed and reopened, and a `manual` prompt also resets it. Expect at most one `startup` impression per hour per user, not one per launch.
- Desktop never fires this event (the checker is a no-op and the row is hidden).
