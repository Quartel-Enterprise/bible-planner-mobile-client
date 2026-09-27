# annotations_search_opened

**Tier:** P2 | **Domain:** Verse annotations

Opening the search on the Annotations screen, on the phone layout.

## When it fires

The user taps the magnifying glass in the top bar.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnSearchClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- The wide layout keeps the search field always visible in the header, so it never fires this event.
- Typing is not tracked: it is a continuous signal, not a click.
