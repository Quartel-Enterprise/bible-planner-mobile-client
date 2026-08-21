# highlight_custom_color_locked_clicked

**Tier:** P2 | **Domain:** Verse annotations

A free user reached for the custom-colour swatch and hit the Pro lock instead of the picker. This is the demand signal for the paywall shown from the highlight palette.

## When it fires

The user taps the multicoloured swatch at the end of the palette row while not subscribed to Pro.

## Trigger source

`feature/verse/selection_menu/.../presentation/VerseSelectionViewModel.kt` — `VerseSelectionUiEvent.OnCustomColorPickerOpen`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Mutually exclusive with [highlight_color_picker_opened](highlight_color_picker_opened.md) for the same tap: a Pro user opens the picker, a free user gets this event instead.
- Always immediately followed by a navigation to the paywall (`PaywallNavRoute` with source `highlight_custom_color`).
