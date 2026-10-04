# verse_note_delete_opened

**Tier:** P2 | **Domain:** Verse annotations

The confirmation to delete a verse note was opened.

## When it fires

The user taps Delete note in the verse note editor, which only shows for a note that already exists.

## Trigger source

`feature/verse/add_note/.../presentation/VerseNoteViewModel.kt` — `VerseNoteUiEvent.OnDeleteClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Ends in [verse_note_deleted](verse_note_deleted.md) or [verse_note_delete_cancelled](verse_note_delete_cancelled.md).
