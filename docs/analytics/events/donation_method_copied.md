# donation_method_copied

**Tier:** P1 | **Domain:** Monetization

Captures the user copying a donation address/key to the clipboard — the strongest available intent signal for a donation, since crypto and Pix payments happen outside the app and cannot be observed directly.

## When it fires

The user taps the copy button on a donation method in the donation bottom sheet and the value is actually copied to the clipboard.

## Trigger source

`feature/donation/.../presentation/DonationViewModel.kt` — `DonationUiEvent.Copy(type)` (only the branch where `copiedType != event.type`, i.e. a real copy happens)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `btc_lightning` | `btc_onchain` \| `btc_lightning` \| `usdt_erc20` \| `usdt_trc20` \| `pix` (snake_case of `DonationType`) |

## Notes

- Tapping the same method again while it is in "copied" state only clears the copied indicator without copying — that branch must not be logged.
- This is intent, not a completed donation; there is no way to confirm payment.
- Related: [donation_section_toggled](donation_section_toggled.md), [github_sponsors_opened](github_sponsors_opened.md), [pix_qr_shared](pix_qr_shared.md).
