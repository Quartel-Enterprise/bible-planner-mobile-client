# verse_share_pro_option_locked_clicked

**Tier:** P2 | **Domain:** Verse annotations

A free user reached for a Pro-only option of the image composer and hit the paywall gate instead. This is the demand signal for the paywall shown from the share card.

## When it fires

A user without Pro taps the multicoloured swatch, the photo swatch or Remove mark.

## Trigger source

`feature/verse/share/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnCustomColorClick`, `ShareVerseUiEvent.OnPhotoBackgroundClick` and `ShareVerseUiEvent.OnWatermarkToggleClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `option` | string | `watermark` | `custom_color`, `photo` or `watermark` |

## Notes

- Always immediately followed by a navigation to the paywall teaser (`PaywallTeaserNavRoute` with reason `share_image_style`), whose subscribe button opens the paywall with source `share_image` — see [paywall_teaser_subscribe_clicked](paywall_teaser_subscribe_clicked.md).
