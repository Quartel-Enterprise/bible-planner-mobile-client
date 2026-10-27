# verse_share_color_picker_toggled

**Tier:** P2 | **Domain:** Verse annotations

A Pro user opened or closed the custom colour picker of the card background.

## When it fires

A Pro user taps the multicoloured swatch in the image composer. Opening it also switches the card to the custom colour.

## Trigger source

`feature/verse/share/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnCustomColorClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_open` | string | `"true"` | Whether the picker is open after the tap |

## Notes

- Dragging the hue and lightness sliders is not tracked (it fires on every step); the switch to the custom colour is reported by [verse_share_style_changed](verse_share_style_changed.md) with `background=custom`.
- A free user gets [verse_share_pro_option_locked_clicked](verse_share_pro_option_locked_clicked.md) with `option=custom_color` instead.
