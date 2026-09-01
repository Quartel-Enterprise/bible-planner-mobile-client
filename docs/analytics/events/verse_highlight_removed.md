# verse_highlight_removed

**Tier:** P1 | **Domain:** Verse annotations

A highlight was cleared. There is no separate remove button in the design, so this is the only way to tell deliberate un-highlighting from a colour change.

## When it fires

The user taps the palette colour that every selected verse already carries.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnHighlightColorClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `color` | string | `yellow` | The colour key: a preset name, or `c:<hue>:<lightness>` for a custom mix |
| `is_custom` | boolean | false | Whether the colour was one the user mixed rather than a preset |
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- The removal is stored as a tombstone (a null colour with a fresh timestamp) so it reaches the user's other devices.
