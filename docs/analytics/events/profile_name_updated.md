# profile_name_updated

**Tier:** P1 | **Domain:** Settings

Captures the user saving a new display name. This is the completion signal for the name-editing flow.

## When it fires

User taps "Save" in the name dialog. Fires on tap; a blank name is discarded by the ViewModel without writing, so a small share of these will not result in a stored change.

## Trigger source

`feature/edit_profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/editprofile/presentation/viewmodel/EditNameViewModel.kt` — `EditNameUiEvent.OnSaveClick`

## Parameters

None. The name itself is personal data and is deliberately not sent.

## Notes

- The write is offline-first: it lands in the local database immediately and syncs later, so this event does not confirm the change reached the backend.
- Related: [edit_profile_name_clicked](edit_profile_name_clicked.md).
