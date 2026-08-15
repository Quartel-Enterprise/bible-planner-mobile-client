# highlight_custom_color_created

**Tier:** P1 | **Domain:** Verse annotations

A colour the user mixed themselves entered their palette. Together with `color` across users this says which hues the presets are missing.

## When it fires

The user taps Apply colour in the custom colour picker. The colour is added to the palette and applied to the selection in the same step.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnCustomColorApplyClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `color` | string | `yellow` | The colour key: a preset name, or `c:<hue>:<lightness>` for a custom mix |

## Notes

- Always immediately followed by [verse_highlight_applied](verse_highlight_applied.md) with `is_custom=true`.
- The palette keeps only the four most recent mixes; an evicted colour is not reported, and highlights made with it keep rendering because the colour components live in the highlight itself.
