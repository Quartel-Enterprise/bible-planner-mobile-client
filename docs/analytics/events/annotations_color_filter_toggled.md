# annotations_color_filter_toggled

**Tier:** P2 | **Domain:** Verse annotations

Filtering the list by a highlight colour. One event with an `is_selected` boolean, so selecting and clearing a colour stay comparable.

## When it fires

The user taps a colour dot in the filter bar (phone) or the filter panel (wide layout). Tapping the selected colour again clears the colour filter.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnColorFilterClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `color` | string | yellow | Key of the tapped colour — a preset name or a custom `c:<hue>:<lightness>` key |
| `is_selected` | boolean | true | Whether the tap selected the colour or cleared it |

## Notes

- Tracked manually because `is_selected` depends on the filter that was active before the tap.
