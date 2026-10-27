# verse_share_photo_picker_opened

**Tier:** P2 | **Domain:** Verse annotations

A Pro user opened the system photo picker to use one of their pictures as the card background.

## When it fires

A Pro user taps the photo swatch in the image composer.

## Trigger source

`feature/verse/share/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnPhotoBackgroundClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- A picked photo is reported by [verse_share_style_changed](verse_share_style_changed.md) with `background=photo`; cancelling the picker reports nothing.
- A free user gets [verse_share_pro_option_locked_clicked](verse_share_pro_option_locked_clicked.md) with `option=photo` instead.
