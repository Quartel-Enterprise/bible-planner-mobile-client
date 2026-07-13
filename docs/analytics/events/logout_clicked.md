# logout_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping the logout row/button, the entry point into the logout confirmation flow. It is emitted from two different modules with different `source` values, so the funnel can distinguish where the logout attempt started.

## When it fires

User taps the logout row on the More screen, or the logout action on the Account Details screen.

## Trigger source

- `feature/more/src/commonMain/kotlin/com/quare/bibleplanner/feature/more/presentation/viewmodel/MoreViewModel.kt` — `MoreUiEvent.OnLogoutClick` (`source = "more_menu"`)
- `feature/account_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/accountdetails/presentation/viewmodel/AccountDetailsViewModel.kt` — `AccountDetailsUiEvent.OnLogoutClick` (`source = "account_details"`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `more_menu` | Which screen the logout was triggered from: `more_menu` \| `account_details` |

## Notes

- Both trigger points navigate to `LogoutNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`logout`, `dialog`).
- The actual confirmation/cancellation on that dialog is covered by [logout_confirmed](logout_confirmed.md) and [logout_cancelled](logout_cancelled.md).
