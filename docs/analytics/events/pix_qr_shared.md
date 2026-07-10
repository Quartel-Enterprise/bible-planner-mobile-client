# pix_qr_shared

**Tier:** P1 | **Domain:** Monetization

Captures the user sharing the Pix donation QR code through the system share sheet. Beyond donation intent, the share message includes the app store link, so this doubles as an organic-referral signal.

## When it fires

The user taps share in the Pix QR dialog, opening the system share sheet with the QR code and a localized message containing the store link.

## Trigger source

`feature/donation/pix_qr/.../presentation/PixQrViewModel.kt` — `PixQrUiEvent.Share`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Logged when the share sheet is requested; whether the user completes the share in the system UI is not observable.
- Pix QR dialog impressions are covered by [destination_view](destination_view.md) (`destination_name=pix_qr`), reached via `DonationUiEvent.OpenPixQr`.
- Related: [donation_method_copied](donation_method_copied.md) with `method=pix` for the copy-key alternative.
