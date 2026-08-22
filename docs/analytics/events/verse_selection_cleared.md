# verse_selection_cleared

**Tier:** P2 | **Domain:** Verse annotations

The user dismissed the selection panel without highlighting, saving, noting, copying or sharing — the drop-off point of the annotation funnel.

## When it fires

The user taps the close button on the selection panel.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnClearSelectionClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Tapping the last selected verse a second time also empties the selection, but reports [verse_selection_toggled](verse_selection_toggled.md) with `verse_count=0` instead.
