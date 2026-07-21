# profile_photo_updated

**Tier:** P1 | **Domain:** Settings

Captures the user confirming the crop and committing a new profile photo. This is the completion signal for the photo-upload flow.

## When it fires

User taps "Done" on the "Adjust photo" crop screen.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/CropPhotoViewModel.kt` — `CropPhotoUiEvent.OnConfirmClick`

## Parameters

None.

## Notes

- Fires on tap, before the image is encoded. A decode failure afterwards surfaces an error to the user, so a small share of these will not result in a stored photo.
- The write is offline-first: the cropped bytes land in the local database immediately and upload to storage later, so this event does not confirm the photo reached the backend.
- Pairs with [profile_photo_source_selected](profile_photo_source_selected.md) to measure how many picks survive the crop step.
