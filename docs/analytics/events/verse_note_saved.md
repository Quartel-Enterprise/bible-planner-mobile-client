# verse_note_saved

**Tier:** P1 | **Domain:** Verse annotations

A note about a passage was written or edited. The completion signal of the note funnel, and `note_length` says whether people write a line or a paragraph.

## When it fires

The user taps Save in the verse note editor.

## Trigger source

`feature/verse_note/.../presentation/VerseNoteViewModel.kt` — `VerseNoteUiEvent.OnSaveClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `note_length` | integer | 128 | Character count of the saved note |
| `verse_count` | integer | 3 | How many verses the selection covered |
| `is_existing` | boolean | false | Whether the passage already had a note |

## Notes

- Saving an emptied note deletes it instead; the event still fires, with `note_length=0`.
- Distinct from [note_saved](note_saved.md), which is the day-level note on the plan screen.
