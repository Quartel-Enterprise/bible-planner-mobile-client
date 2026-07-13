# account_card_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping the account card on the More screen, the entry point into account management (renaming/signing out devices, logging out) from the settings hub.

## When it fires

User taps the account card on the More screen.

## Trigger source

`feature/more/src/commonMain/kotlin/com/quare/bibleplanner/feature/more/presentation/viewmodel/MoreViewModel.kt` — `MoreUiEvent.OnAccountCardClick`

## Parameters

None.

## Notes

- Navigates to `AccountDetailsNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`account_details`, `responsive`).
- Related: [pro_card_clicked](pro_card_clicked.md), [login_row_clicked](login_row_clicked.md), [rename_device_clicked](rename_device_clicked.md).
