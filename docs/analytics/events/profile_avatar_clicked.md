# profile_avatar_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping their avatar on the Profile screen, which opens the expanded photo overlay with quick photo actions.

## When it fires

User taps the avatar image on the profile card in the Profile screen.

## Trigger source

`feature/profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/profile/presentation/viewmodel/ProfileViewModel.kt` — `ProfileUiEvent.OnAvatarClick`

## Parameters

None.

## Notes

- Distinct from [edit_profile_clicked](edit_profile_clicked.md): tapping the avatar jumps straight to the photo shortcuts, while the edit button opens the full edit sheet.
- Navigates to `ExpandedPhotoNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`profile_photo_expanded`, `dialog`).
