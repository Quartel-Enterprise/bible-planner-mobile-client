# login_nudge_shown

**Tier:** P1 | **Domain:** Auth

Captures an impression of the login-sync nudge — the dialog that suggests signing in to back up progress after a logged-out user performs a sync-worthy action (favoriting a book, marking a chapter/day as read, editing the plan start date). Denominator for the nudge funnel: how often the nudge appears vs how often it converts.

## When it fires

`RequestLoginNudgeIfNeeded` decides the nudge is due (`ShouldShowLoginNudge` passes: logged out, not snoozed, not permanently disabled) and navigates to `LoginSyncNudgeNavRoute`.

## Trigger source

`core/login_nudge/.../RequestLoginNudgeIfNeededUseCase.kt` — the `navigationEventBus.send(LoginSyncNudgeNavRoute)` branch. Not a ViewModel `UiEvent`: the use case is invoked after qualifying actions by `ReadViewModel`, `DayViewModel`, `BooksViewModel`, `BookDetailsViewModel`, `ReadingPlanViewModel` and `EditPlanStartDateViewModel`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Logging inside the use case (rather than in each calling ViewModel) keeps one trigger point; the impression is also visible as `destination_view` with `destination_name=login_sync_nudge`, but this dedicated event anchors the nudge funnel.
- Outcomes: [login_nudge_accepted](login_nudge_accepted.md), [login_nudge_snoozed](login_nudge_snoozed.md), [login_nudge_disabled](login_nudge_disabled.md).
