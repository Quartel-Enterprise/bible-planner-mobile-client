# annotations_search_cleared

**Tier:** P2 | **Domain:** Verse annotations

Clearing the text typed in the Annotations search.

## When it fires

The user taps the X inside the search field.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnSearchClearClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- The field stays open (phone) or in place (wide layout); only the text goes away.
