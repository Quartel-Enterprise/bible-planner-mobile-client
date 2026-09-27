# annotation_removal_confirmed

**Tier:** P1 | **Domain:** Verse annotations

Removing everything marked on a passage from the Annotations screen: its highlight, its bookmark and its note.

## When it fires

The user confirms the removal dialog.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnRemoveConfirm`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 2 | How many verses the passage covers |
| `has_highlight` | boolean | true | Whether a highlight was removed |
| `is_saved` | boolean | false | Whether a bookmark was removed |
| `has_note` | boolean | true | Whether a note was removed |

## Notes

- The verse text itself is untouched; only the user's marks go away, and the removal syncs to the other devices.
