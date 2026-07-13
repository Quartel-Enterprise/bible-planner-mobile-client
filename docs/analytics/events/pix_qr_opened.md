# pix_qr_opened

**Tier:** P2 | **Domain:** Monetization

Captures the user opening the Pix QR code screen from the donation bottom sheet, the next step in the Pix donation funnel after expanding the Pix section.

## When it fires

User taps the action to view the Pix QR code on the donation bottom sheet.

## Trigger source

`feature/donation/src/commonMain/kotlin/com/quare/bibleplanner/feature/donation/presentation/DonationViewModel.kt` — `DonationUiEvent.OpenPixQr`

## Parameters

None.

## Notes

- Navigates to `PixQrNavRoute`; destination impression is covered by [destination_view](destination_view.md) (`pix_qr`, `dialog`).
- Related: [pix_qr_shared](pix_qr_shared.md), [pix_qr_dismissed](pix_qr_dismissed.md), [donation_section_toggled](donation_section_toggled.md).
