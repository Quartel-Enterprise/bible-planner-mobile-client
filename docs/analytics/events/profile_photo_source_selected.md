# profile_photo_source_selected

**Tier:** P1 | **Domain:** Settings

Captures which source the user picked for their profile photo, so we can see whether people prefer reusing their provider photo or supplying their own.

## When it fires

User taps one of the photo source options, either in the photo source sheet or in the expanded photo overlay shortcuts.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/ProfilePhotoViewModel.kt` — `ProfilePhotoUiEvent.OnUseProviderPhotoClick`, `OnPickFromGalleryClick`, `OnTakePhotoClick`, `OnPickFromGalleryClick`, `OnTakePhotoClick`

## Parameters

| Parameter | Type | Values | Description |
|---|---|---|---|
| `source` | string | `provider` \| `gallery` \| `camera` | Which photo source the user chose |

## Notes

- `provider` means falling back to the photo from the OAuth account (Google); the option is hidden when the provider supplies no photo, so Apple accounts never emit it.
- `camera` is not available on desktop, so it is absent from that platform.
- Choosing `gallery` or `camera` only opens the picker — it does not mean a photo was set.
- Related: [profile_photo_removed](profile_photo_removed.md), [edit_profile_photo_clicked](edit_profile_photo_clicked.md).
