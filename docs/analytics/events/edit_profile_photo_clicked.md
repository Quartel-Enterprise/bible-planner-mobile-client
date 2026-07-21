# edit_profile_photo_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user choosing "Change image" in the edit profile sheet — the intent step before the photo source sheet is shown.

## When it fires

User taps the "Change image" row in the edit profile sheet.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/EditProfileViewModel.kt` — `EditProfileUiEvent.OnChangeImageClick`

## Parameters

None.

## Notes

- Pairs with [profile_photo_source_selected](profile_photo_source_selected.md) to measure how many users who open the photo sheet actually pick a source.
- Navigates to `EditPhotoSourceNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`edit_profile_photo_source`, `responsive`).
