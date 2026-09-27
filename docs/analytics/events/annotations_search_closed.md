# annotations_search_closed

**Tier:** P2 | **Domain:** Verse annotations

Closing the Annotations search on the phone layout, which also drops the query.

## When it fires

The user taps the back arrow inside the search field.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnSearchCloseClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- The other filters stay as they were.
