# verse_note_delete_cancelled

**Tier:** P2 | **Domain:** Verse annotations

The confirmation to delete a verse note was dismissed without deleting it.

## When it fires

The user taps Cancel in the delete confirmation, or dismisses it.

## Trigger source

`feature/verse/add_note/.../presentation/VerseNoteViewModel.kt` — `VerseNoteUiEvent.OnDeleteCancel`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Counterpart of [verse_note_deleted](verse_note_deleted.md); both follow [verse_note_delete_opened](verse_note_delete_opened.md).
