# edit_profile_name_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user choosing "Change name" in the edit profile sheet — the intent step before the name dialog is shown.

## When it fires

User taps the "Change name" row in the edit profile sheet.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/EditProfileViewModel.kt` — `EditProfileUiEvent.OnChangeNameClick`

## Parameters

None.

## Notes

- Pairs with [profile_name_updated](profile_name_updated.md) to measure the drop-off between opening the name dialog and actually saving.
- Navigates to `EditNameNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`edit_profile_name`, `dialog`).
