# verse_share_style_changed

**Tier:** P2 | **Domain:** Verse annotations

The card was restyled before sharing. Which backgrounds and typefaces get chosen says which ones are worth keeping.

## When it fires

The user taps a background swatch or a typeface tile in the image composer, opens the custom colour (Pro), or picks a photo as the background (Pro).

## Trigger source

`feature/verse/share/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnBackgroundClick`, `ShareVerseUiEvent.OnFontClick`, `ShareVerseUiEvent.OnCustomColorClick` and `ShareVerseUiEvent.OnPhotoPicked`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `background` | string | `forest` | The card background in use: a preset name, `custom` or `photo` |
| `font` | string | `lora` | The typeface in use |

## Notes

- Both parameters carry the full style after the change, so the last event before [verse_shared](verse_shared.md) describes the card that was shared.
