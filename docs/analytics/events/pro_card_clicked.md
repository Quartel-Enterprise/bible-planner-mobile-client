# pro_card_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping the Pro subscription card on the Profile screen. It is the entry point into the subscription-management funnel from the settings hub, separate from the paywall entry points reached via `profile_option_clicked`'s `become_pro` option.

## When it fires

User taps the Pro card on the Profile screen.

## Trigger source

`feature/profile/src/commonMain/kotlin/com/quare/bibleplanner/feature/profile/presentation/viewmodel/ProfileViewModel.kt` — `ProfileUiEvent.OnProCardClick`

## Parameters

None.

## Notes

- Navigates to `SubscriptionDetailsNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`subscription_details`, `dialog`).
- Related: [account_card_clicked](account_card_clicked.md), [login_row_clicked](login_row_clicked.md), [logout_clicked](logout_clicked.md), [profile_option_clicked](profile_option_clicked.md).
