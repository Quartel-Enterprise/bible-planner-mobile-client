# verse_share_watermark_toggled

**Tier:** P2 | **Domain:** Verse annotations

A Pro user removed the Bible Planner mark from the card, or put it back. Says how much the Pro option is actually used.

## When it fires

A Pro user taps Remove mark or Show mark in the image composer.

## Trigger source

`feature/verse/share/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnWatermarkToggleClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | string | `"false"` | Whether the mark is on the card after the tap |

## Notes

- A free user tapping Remove mark gets [verse_share_pro_option_locked_clicked](verse_share_pro_option_locked_clicked.md) with `option=watermark` instead.
