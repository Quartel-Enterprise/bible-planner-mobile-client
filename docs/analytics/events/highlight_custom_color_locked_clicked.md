# highlight_custom_color_locked_clicked

**Tier:** P2 | **Domain:** Verse annotations

A free user reached for a Pro-only colour — the custom swatch, or one of the three locked presets — and hit the paywall gate instead of applying it. This is the demand signal for the paywall shown from the highlight palette.

## When it fires

The user taps the multicoloured custom swatch, or a locked preset (the 4th, 5th or 6th colour dot — `PresetHighlightColor.requiresPro`), while not subscribed to Pro.

## Trigger source

`feature/verse/selection_menu/.../presentation/VerseSelectionViewModel.kt` — `VerseSelectionUiEvent.OnCustomColorPickerOpen` (custom swatch) and `VerseSelectionUiEvent.OnLockedColorClick` (locked preset), both routed through the same `openColorPaywall()`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Mutually exclusive with [highlight_color_picker_opened](highlight_color_picker_opened.md) for the custom swatch: a Pro user opens the picker, a free user gets this event instead. Locked presets never open anything for a free user — this event is the only outcome.
- Always immediately followed by a navigation to the paywall teaser (`PaywallTeaserNavRoute` with reason `highlight_custom_color`), not the paywall itself — see [paywall_teaser_subscribe_clicked](paywall_teaser_subscribe_clicked.md).
