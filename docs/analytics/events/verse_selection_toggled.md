# verse_selection_toggled

**Tier:** P2 | **Domain:** Verse annotations

Counts taps on a verse in the reader, which is how the selection that drives every annotation action is built. The pair of this and the actions that follow it is the funnel for the whole highlight/save/note feature.

## When it fires

The user taps a verse row. Selecting a verse in a different chapter (vertical reading keeps two on screen) replaces the selection rather than extending it.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnVerseClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_selected` | boolean | true | Whether the tap added the verse to the selection or removed it |
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- `verse_count` is the size of the selection *after* the tap, so a deselection that empties it reports 0.
- Followed by [verse_selection_cleared](verse_selection_cleared.md) when the panel is closed without acting.
