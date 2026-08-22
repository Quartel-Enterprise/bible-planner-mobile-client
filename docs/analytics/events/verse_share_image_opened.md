# verse_share_image_opened

**Tier:** P2 | **Domain:** Verse annotations

The user chose the image card over plain text, reaching the composer.

## When it fires

The user taps As an image in the share sheet.

## Trigger source

`feature/share_verse/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnShareAsImageClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- Followed by [verse_shared](verse_shared.md) with `format=image` when the card is actually shared.
