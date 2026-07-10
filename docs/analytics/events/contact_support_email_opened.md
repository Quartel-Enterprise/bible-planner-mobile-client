# contact_support_email_opened

**Tier:** P1 | **Domain:** Settings

Captures the user opening a pre-filled support email from the contact-support sheet. Support contact volume, segmented by login and Pro status, shows who needs help and whether paying users hit more friction.

## When it fires

User taps "send email" in the contact-support sheet; the ViewModel builds the mailto link with diagnostic context and opens the mail client.

## Trigger source

`feature/contact_support/src/commonMain/kotlin/com/quare/bibleplanner/feature/contactsupport/presentation/viewmodel/ContactSupportViewModel.kt` — `ContactSupportUiEvent.OnSendEmailClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_logged_in` | boolean | `true` | Whether the user is authenticated, derived from `ContactSupportUiState.accountStatusModel` (`AccountStatusModel.LoggedIn` vs `LoggedOut`) |
| `is_pro` | boolean | `false` | Standard param, derived from `ContactSupportUiState.subscriptionStatus` (`Loadable<SubscriptionStatus?>`) |

## Notes

- The UiState does not carry these booleans directly — instrumentation derives them from `accountStatusModel` and `subscriptionStatus`; while either is still `Loading` the value should be omitted rather than defaulted.
- Opening the mail client is fire-and-forget: this does not confirm an email was actually sent.
- Related: [contact_support_email_copied](contact_support_email_copied.md); sheet impressions are covered by [destination_view](destination_view.md) (`contact_support`).
