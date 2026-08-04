# profile_photo_crop_transformed

**Tier:** P2 | **Domain:** Settings

Captures the user mirroring or rotating the photo on the crop screen, so we can see whether these controls are worth keeping and which one is actually used.

## When it fires

User taps "Horizontal", "Vertical" or "Rotate" on the "Adjust photo" crop screen. Every tap emits an event, including the ones that undo a previous tap.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/CropPhotoViewModel.kt` — `CropPhotoUiEvent.OnFlipHorizontalClick`, `OnFlipVerticalClick`, `OnRotateClick`

## Parameters

| Parameter | Type | Values | Description |
|---|---|---|---|
| `transform` | string | `flip_horizontal` \| `flip_vertical` \| `rotate` | Which control the user tapped |

## Notes

- `rotate` is always a 90° clockwise quarter turn; four taps return to the original orientation.
- The event says the control was used, not that the photo was saved — pair it with [profile_photo_updated](profile_photo_updated.md) and [profile_photo_crop_cancelled](profile_photo_crop_cancelled.md) to know the outcome.
