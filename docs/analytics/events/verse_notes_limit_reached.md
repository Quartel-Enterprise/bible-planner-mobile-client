# verse_notes_limit_reached

**Tier:** P1 | **Domain:** Verse annotations

Captures a free user hitting the verse notes limit — the moment the free-tier gate on verse notes becomes visible. It is the top of the verse-notes-driven upgrade funnel, kept apart from [notes_limit_reached](notes_limit_reached.md) (day notes) so each gate's conversion can be read on its own.

## When it fires

A free user asks to write a new note on a passage that has none while already at the maximum number of free verse notes, and the `verse_notes_limit_enabled` Remote Config flag is on. The app opens the AddNotesFreeWarning dialog (`type=verse`) instead of the note editor.

## Trigger source

- `feature/verse/selection_menu/.../presentation/VerseSelectionViewModel.kt` — `VerseSelectionUiEvent.OnNoteClick` (blocked path)
- `feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnNoteClick` (blocked path, fired alongside the automatic [annotation_note_opened](annotation_note_opened.md))

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `max_free_notes` | int | `3` | Free-tier verse notes limit in effect (`max_free_verse_notes` in Remote Config) |
| `source` | string | `selection_menu` | Where the note was requested: `selection_menu` (verse selection panel) or `annotations` (Annotations screen) |

## Notes

- `ShouldBlockAddVerseNote` gates on `verse_notes_limit_enabled && isFreeUser() && liveVerseNotes >= max_free_verse_notes`, so this event never fires for Pro users or while the flag is off.
- Opening a passage that already has a note never blocks (editing existing notes is always allowed) and must not fire this event.
- The click itself still logs [verse_note_opened](verse_note_opened.md) / [annotation_note_opened](annotation_note_opened.md); this event marks that the click hit the wall instead of reaching the editor.
- The dialog's own impression is covered by [screen_view](screen_view.md) (`add_notes_free_warning`, `type=verse`). Follow-up: [notes_limit_subscribe_clicked](notes_limit_subscribe_clicked.md) → [paywall_viewed](paywall_viewed.md) with `source=verse_notes_limit`.
