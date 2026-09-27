# annotations_type_filter_changed

**Tier:** P2 | **Domain:** Verse annotations

Which kind of annotation the user narrows the list to. Shows whether people come to this screen for their highlights, their bookmarks or their notes.

## When it fires

The user taps one of the type chips (phone) or type rows (wide layout): All, Highlights, Saved or Notes. Tapping the selected type again clears it back to All.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnTypeFilterClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `filter_type` | string | highlights | The type applied after the tap: `all`, `highlights`, `saved` or `notes` |

## Notes

- Tracked manually because the applied type depends on the one selected before the tap: re-tapping it reports `all`.
