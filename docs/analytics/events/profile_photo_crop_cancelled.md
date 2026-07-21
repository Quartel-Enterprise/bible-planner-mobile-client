# profile_photo_crop_cancelled

**Tier:** P2 | **Domain:** Settings

Captures the user leaving the crop screen without saving, either via the close button or the "Cancel" button. Pairs with [profile_photo_updated](profile_photo_updated.md) to measure how many picks are abandoned at the crop step.

## When it fires

User taps the close (X) button or the "Cancel" button on the "Adjust photo" crop screen.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/CropPhotoViewModel.kt` — `CropPhotoUiEvent.OnCancelClick`

## Parameters

None.

## Notes

- Both dismiss controls (header X and footer button) emit the same event.
