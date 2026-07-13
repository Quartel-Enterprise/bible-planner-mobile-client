# notes_clear_clicked

**Tier:** P2 | **Domain:** Notes

Captures the user tapping the clear-notes control on the Day screen. This click also triggers navigation to the delete-notes confirmation dialog (which `destination_view` logs separately), so this event isolates click-through intent from the resulting screen-view/confirmation funnel.

## When it fires

User taps the clear-notes control on the Day screen, opening the delete-notes confirmation dialog.

## Trigger source

`feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/model/DayUiEvent.kt` — `DayUiEvent.OnNotesClear`

## Parameters

None.

## Notes

- Related: [destination_view](destination_view.md) (`delete_notes` with `plan_type`, `week_number`, `day_number`) fires separately once the confirmation dialog becomes visible; the actual deletion is [note_deleted](note_deleted.md) or its cancellation is [note_delete_cancelled](note_delete_cancelled.md).
