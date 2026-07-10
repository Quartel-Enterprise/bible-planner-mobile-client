# progress_reset_confirmed

**Tier:** P1 | **Domain:** Reading

Captures a user wiping all reading progress. A rare, destructive action — it either means starting the plan over (re-engagement) or abandoning accumulated progress (churn risk). Worth alerting on if volume spikes.

## When it fires

User taps the confirm button in the delete-all-progress dialog and the reset succeeds.

## Trigger source

`feature/delete_progress/src/commonMain/kotlin/com/quare/bibleplanner/feature/deleteprogress/presentation/viewmodel/DeleteAllProgressViewModel.kt` — `DeleteAllProgressUiEvent.OnConfirmDelete`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- The handler catches failures from `resetAllProgress()` and returns the dialog to idle — log only after the use case succeeds, not on tap.
- After a reset, the high-water marks for [bible_progress_milestone](bible_progress_milestone.md) must be cleared, and derived events ([book_completed](book_completed.md), [week_completed](week_completed.md), [plan_completed](plan_completed.md)) must not fire from the reset write itself.
- Pair: [progress_reset_cancelled](progress_reset_cancelled.md). Dialog impressions come from [destination_view](destination_view.md) (`delete_all_progress`), so confirm-rate is impressions vs this event.
