# note_saved

**Tier:** P1 | **Domain:** Notes

Captures a day note being persisted. Notes are a sticky engagement feature and the gate for the free-tier limit, so save volume feeds both retention and monetization analysis.

## When it fires

The debounced auto-save commits the note text: 2 seconds after the user stops typing in the Day screen notes field, or on the flush when the screen is closed with a save still pending (`onCleared`).

## Trigger source

`feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/viewmodel/DayViewModel.kt` — `DayUiEvent.OnNotesChanged` (debounced via `notesSaveJob`; also the `onCleared` flush through `applicationScope`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `plan_type` | string | `chronological` | Active reading plan |
| `week_number` | int | `12` | 1-based week of the day the note belongs to |
| `day_number` | int | `3` | 1-based day within the week |
| `note_length` | int | `142` | Character count of the saved note |

## Notes

- Log per committed save, never per keystroke — the 2s debounce (`notesDebounceDelay`) is the natural rate limit, but a long editing session still produces multiple saves; that is acceptable.
- `saveNotes` converts blank text to `null`, which effectively deletes the note — do not log `note_saved` for blank saves (see [note_deleted](note_deleted.md)).
- The silent cleanup in `deleteNotesAsyncDueToBlockedAddNotes` (free-limit race) must not log anything.
- Related: [notes_limit_reached](notes_limit_reached.md) fires when a free user cannot start a new note.
