# contact_support_email_copied

**Tier:** P2 | **Domain:** Settings

Captures the user copying the support email address to the clipboard instead of opening the mail client. The fallback path for users without a configured mail app — its share of total contact attempts tells us how important the copy affordance is.

## When it fires

User taps the copy-email button in the contact-support sheet; the address is copied and a confirmation snackbar is shown.

## Trigger source

`feature/contact_support/src/commonMain/kotlin/com/quare/bibleplanner/feature/contactsupport/presentation/viewmodel/ContactSupportViewModel.kt` — `ContactSupportUiEvent.OnCopyEmailClick`

## Parameters

None.

## Notes

- Unlike [contact_support_email_opened](contact_support_email_opened.md), the copied email carries no diagnostic context, so support tickets from this path arrive without version/platform info.
