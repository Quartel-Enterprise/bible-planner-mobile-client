# annotation_note_opened

**Tier:** P1 | **Domain:** Verse annotations

Opening the note editor from the Annotations screen, either to write a first note on a highlighted or saved passage or to edit an existing one.

## When it fires

The user picks Add note or Edit note in a row's menu.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnNoteClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_existing` | string | `"true"` | Whether the passage already had a note |
| `verse_count` | integer | 2 | How many verses the passage covers |

## Notes

- The editor is the same one the reader opens; saving still fires [verse_note_saved](verse_note_saved.md).
