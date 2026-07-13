# donation_dismissed

**Tier:** P2 | **Domain:** Monetization

Captures the user closing the donation bottom sheet without copying a donation method or opening an external donation link. Gives the drop-off count for the donation funnel alongside [donation_method_copied](donation_method_copied.md), [github_sponsors_opened](github_sponsors_opened.md), and [pix_qr_opened](pix_qr_opened.md).

## When it fires

User dismisses the donation bottom sheet.

## Trigger source

`feature/donation/src/commonMain/kotlin/com/quare/bibleplanner/feature/donation/presentation/DonationViewModel.kt` — `DonationUiEvent.Dismiss`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [destination_view](destination_view.md) (`donation`, `bottom_sheet`).
- Related: [donation_method_copied](donation_method_copied.md), [donation_section_toggled](donation_section_toggled.md), [pix_qr_opened](pix_qr_opened.md).
