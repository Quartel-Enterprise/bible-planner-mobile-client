# verse_note_opened

**Tier:** P1 | **Domain:** Verse annotations

The note editor was opened for a passage. `is_existing` separates writing a new note from returning to one.

## When it fires

The user taps Note (or View note, when the selection touches a note) in the selection panel.

## Trigger source

`feature/verse/selection_menu/.../presentation/VerseSelectionViewModel.kt` — `VerseSelectionUiEvent.OnNoteClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_existing` | string | `"false"` | Whether the selection touched a note, which then opens over that note's own verses |
| `verse_count` | integer | 3 | How many verses the opened note covers: the note's verses when it exists, the selection otherwise |

## Notes

- The editor's own impression is covered by [screen_view](screen_view.md) (`screen_name=verse_note`); this measures click-through rate, which is a different number.
- Ends in [verse_note_saved](verse_note_saved.md), [verse_note_deleted](verse_note_deleted.md) or [verse_note_dismissed](verse_note_dismissed.md).
- Tapping the note icon beside a verse opens the same editor and logs [verse_note_icon_clicked](verse_note_icon_clicked.md) instead.
- Fires on the click even when a free user is at the verse notes limit; that case opens the free warning instead of the editor and also logs [verse_notes_limit_reached](verse_notes_limit_reached.md).
