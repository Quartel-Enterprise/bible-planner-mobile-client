# verse_share_image_downloaded

**Tier:** P1 | **Domain:** Verse annotations

The rendered card was saved to the device instead of being sent through the share sheet. Read next to [verse_shared](verse_shared.md) with `format=image`, it says whether people keep the card for later or post it right away.

## When it fires

The user taps the download button beside Share in the image composer.

## Trigger source

`feature/verse/share/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnDownloadImageReady`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- Logged when the save is requested. Android 10+ and iOS write to the photo gallery; older Android versions and the desktop open a save dialog the user may cancel; the web downloads the file.
- iOS asks for the add-only photo library permission on the first save; a refusal shows the save-failed message.
