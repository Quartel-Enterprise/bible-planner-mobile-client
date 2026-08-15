# verse_highlight_applied

**Tier:** P1 | **Domain:** Verse annotations

A verse was marked with a colour. The headline signal for the highlights feature, and `color`/`is_custom` say whether the six presets are enough or people reach for their own mixes.

## When it fires

The user taps a palette colour that at least one of the selected verses does not already carry.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnHighlightColorClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `color` | string | `yellow` | The colour key: a preset name, or `c:<hue>:<lightness>` for a custom mix |
| `is_custom` | boolean | false | Whether the colour was one the user mixed rather than a preset |
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- Applying a custom colour straight from the picker also emits [highlight_custom_color_created](highlight_custom_color_created.md) first.
- Tapping the colour the whole selection already carries removes it instead — see [verse_highlight_removed](verse_highlight_removed.md).
