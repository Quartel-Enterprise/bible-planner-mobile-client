# verse_share_style_changed

**Tier:** P2 | **Domain:** Verse annotations

The card was restyled before sharing. Which backgrounds and typefaces get chosen says which ones are worth keeping.

## When it fires

The user taps a background swatch or a typeface tile in the image composer.

## Trigger source

`feature/share_verse/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnBackgroundClick` and `ShareVerseUiEvent.OnFontClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `background` | string | `forest` | The card background in use |
| `font` | string | `lora` | The typeface in use |

## Notes

- Both parameters carry the full style after the change, so the last event before [verse_shared](verse_shared.md) describes the card that was shared.
