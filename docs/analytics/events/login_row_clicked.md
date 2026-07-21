# login_row_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping the login row on the Profile screen, the entry point into the login flow from the settings hub.

## When it fires

User taps the login row on the Profile screen (shown when the user is signed out).

## Trigger source

`feature/profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/profile/presentation/viewmodel/ProfileViewModel.kt` — `ProfileUiEvent.OnLoginClick`

## Parameters

None.

## Notes

- Navigates to `LoginNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`login`, `bottom_sheet`).
- Related: [account_card_clicked](account_card_clicked.md), [logout_clicked](logout_clicked.md), [login](login.md).
