# profile_photo_removed

**Tier:** P2 | **Domain:** Settings

Captures the user removing their current profile photo, falling back to their initials or the generic avatar.

## When it fires

User taps "Remove current photo" in the photo source sheet, or the "Remove" shortcut in the expanded photo overlay.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/ProfilePhotoViewModel.kt` — `ProfilePhotoUiEvent.OnRemovePhotoClick`

## Parameters

None.

## Notes

- Removal is explicit: it does not fall back to the provider photo. Restoring that is a separate action, tracked as [profile_photo_source_selected](profile_photo_source_selected.md) with `source=provider`.
- The option is only shown when a custom photo is set.
