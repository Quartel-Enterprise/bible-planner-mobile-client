# logout_cancelled

**Tier:** P2 | **Domain:** Auth

Captures backing out of the logout dialog without signing out. Together with [logout_confirmed](logout_confirmed.md) it measures how often the sign-out flow is reached by accident vs intent.

## When it fires

User taps the cancel button in the logout dialog, or dismisses it by other means (e.g. scrim tap, system back) while it is still in its default state.

## Trigger source

`feature/logout/src/commonMain/kotlin/com/quare/bibleplanner/feature/logout/presentation/viewmodel/LogoutViewModel.kt` — two cases, same event:

- `LogoutUiEvent.OnCancel` — explicit cancel button
- `LogoutUiEvent.OnDismiss` — dialog dismissed by other means

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Dialog impressions are covered by [destination_view](destination_view.md) (`destination_name=logout`).
- Pair: [logout_confirmed](logout_confirmed.md).
