# verse_note_opened

**Tier:** P1 | **Domain:** Verse annotations

The note editor was opened for a passage. `is_existing` separates writing a new note from returning to one.

## When it fires

The user taps Note in the selection panel.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnNoteClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_existing` | boolean | false | Whether the passage already had a note |
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- The editor's own impression is covered by [screen_view](screen_view.md) (`screen_name=verse_note`); this measures click-through rate, which is a different number.
- Ends in [verse_note_saved](verse_note_saved.md) or [verse_note_dismissed](verse_note_dismissed.md).
