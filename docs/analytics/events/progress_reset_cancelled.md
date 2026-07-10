# progress_reset_cancelled

**Tier:** P2 | **Domain:** Reading

Captures backing out of the delete-all-progress dialog. Together with [progress_reset_confirmed](progress_reset_confirmed.md) it measures how often the destructive action is reached by accident vs intent.

## When it fires

User taps the cancel button in the delete-all-progress dialog.

## Trigger source

`feature/delete_progress/src/commonMain/kotlin/com/quare/bibleplanner/feature/deleteprogress/presentation/viewmodel/DeleteAllProgressViewModel.kt` — `DeleteAllProgressUiEvent.OnCancel`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Only the explicit cancel button fires this event; dismissing the dialog by other means (system back) should be wired to the same event if it routes through `OnCancel`, otherwise it goes untracked by design.
- Pair: [progress_reset_confirmed](progress_reset_confirmed.md).
