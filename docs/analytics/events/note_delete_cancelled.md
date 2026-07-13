# note_delete_cancelled

**Tier:** P2 | **Domain:** Notes

Captures backing out of the delete-notes dialog. Together with [note_deleted](note_deleted.md) it measures how often the destructive action is reached by accident vs intent.

## When it fires

User taps the cancel button in the delete-notes dialog (reached from the clear-notes button on the Day screen).

## Trigger source

`feature/delete_notes/src/commonMain/kotlin/com/quare/bibleplanner/feature/deletenotes/presentation/viewmodel/DeleteNotesViewModel.kt` — `DeleteNotesUiEvent.OnCancel`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Dialog impressions are covered by [destination_view](destination_view.md) (`destination_name=delete_notes`).
- Pair: [note_deleted](note_deleted.md).
