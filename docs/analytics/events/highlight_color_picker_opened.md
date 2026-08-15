# highlight_color_picker_opened

**Tier:** P2 | **Domain:** Verse annotations

The user reached for a colour outside the six presets, which is the demand signal for the custom palette.

## When it fires

The user taps the multicoloured swatch at the end of the palette row.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnCustomColorPickerOpen`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Ends in either [highlight_custom_color_created](highlight_custom_color_created.md) or [highlight_color_picker_dismissed](highlight_color_picker_dismissed.md).
- Dragging the hue and lightness sliders is not tracked: those are continuous gestures, not decisions.
