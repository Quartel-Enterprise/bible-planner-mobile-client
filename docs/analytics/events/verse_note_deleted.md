# verse_note_deleted

**Tier:** P1 | **Domain:** Verse annotations

An existing verse note was deleted from the note editor.

## When it fires

The user confirms the deletion after tapping Delete note in the verse note editor, which only shows for a note that already exists.

## Trigger source

`feature/verse/add_note/.../presentation/VerseNoteViewModel.kt` — `VerseNoteUiEvent.OnDeleteConfirm`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 3 | How many verses the note covered |

## Notes

- Follows [verse_note_delete_opened](verse_note_delete_opened.md); dismissing the confirmation logs [verse_note_delete_cancelled](verse_note_delete_cancelled.md) instead.
- Saving an emptied note also deletes it, but that path logs [verse_note_saved](verse_note_saved.md) with `note_length=0` instead.
- Removing a whole passage from the annotations screen logs [annotation_removal_confirmed](annotation_removal_confirmed.md).
