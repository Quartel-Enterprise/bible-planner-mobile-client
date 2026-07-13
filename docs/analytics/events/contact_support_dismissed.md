# contact_support_dismissed

**Tier:** P2 | **Domain:** Settings

Captures the user closing the contact-support sheet without emailing or copying the support address. Complements [contact_support_email_opened](contact_support_email_opened.md) and [contact_support_email_copied](contact_support_email_copied.md) to show how often the sheet is opened and dismissed without follow-up.

## When it fires

User dismisses the contact-support sheet (scrim tap, close action, or system back).

## Trigger source

`feature/contact_support/src/commonMain/kotlin/com/quare/bibleplanner/feature/contactsupport/presentation/viewmodel/ContactSupportViewModel.kt` — `ContactSupportUiEvent.OnDismiss`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [destination_view](destination_view.md) (`contact_support`, `responsive`).
- Related: [contact_support_email_opened](contact_support_email_opened.md), [contact_support_email_copied](contact_support_email_copied.md).
