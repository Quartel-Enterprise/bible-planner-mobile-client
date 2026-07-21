# edit_profile_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping the edit button on the profile card in the Profile screen, the entry point into editing their display name and profile photo.

## When it fires

User taps the edit (pencil) button on the profile card in the Profile screen. On desktop the same control renders as an "Edit profile" pill.

## Trigger source

`feature/profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/profile/presentation/viewmodel/ProfileViewModel.kt` — `ProfileUiEvent.OnEditProfileClick`

## Parameters

None.

## Notes

- Navigates to `EditProfileNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`edit_profile`, `responsive`).
- Related: [profile_avatar_clicked](profile_avatar_clicked.md), [account_card_clicked](account_card_clicked.md).
