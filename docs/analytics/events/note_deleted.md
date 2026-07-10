# note_deleted

**Tier:** P1 | **Domain:** Notes

Captures a confirmed deletion of a day's note. Deletion volume relative to [note_saved](note_saved.md) shows whether notes are curated or abandoned, and matters for free users managing their limited note slots.

## When it fires

User taps confirm in the delete-notes dialog (reached from the clear-notes button on the Day screen).

## Trigger source

`feature/delete_notes/src/commonMain/kotlin/com/quare/bibleplanner/feature/deletenotes/presentation/viewmodel/DeleteNotesViewModel.kt` — `DeleteNotesUiEvent.OnConfirmDelete`

The route (`DeleteNotesRoute`) carries `readingPlanType`, `week` and `day`, mapped from the Day screen's `DayUiEvent.OnNotesClear` via `DeleteRouteNotesMapper`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Active reading plan |
| `week_number` | int | `12` | 1-based week of the day whose note was deleted |
| `day_number` | int | `3` | 1-based day within the week |

## Notes

- Only the confirmed dialog path is tracked. Manually erasing all text in the notes field also removes the note (blank saves persist `null` in `DayViewModel.saveNotes`) but bypasses the dialog — that implicit path is untracked by design; revisit if slot-freeing behavior needs full coverage.
- `OnCancel` on this dialog is deliberately not an event; impressions come from [destination_view](destination_view.md) (`delete_notes`).
