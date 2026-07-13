# pix_qr_dismissed

**Tier:** P2 | **Domain:** Monetization

Captures the user closing the Pix QR code screen without sharing the QR code. Complements [pix_qr_shared](pix_qr_shared.md) to show how often viewing the QR code converts into sharing it.

## When it fires

User dismisses the Pix QR code screen.

## Trigger source

`feature/donation/pix_qr/src/commonMain/kotlin/com/quare/bibleplanner/feature/donation/pixqr/presentation/PixQrViewModel.kt` — `PixQrUiEvent.Dismiss`

## Parameters

None.

## Notes

- Destination impression for the screen itself is covered by [destination_view](destination_view.md) (`pix_qr`, `dialog`).
- Related: [pix_qr_shared](pix_qr_shared.md), [pix_qr_opened](pix_qr_opened.md).
