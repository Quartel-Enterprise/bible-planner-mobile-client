# donation_section_toggled

**Tier:** P2 | **Domain:** Monetization

Captures expanding/collapsing the payment-type sections in the donation sheet. Expansion rate per section shows which donation rails users even consider before any copy happens.

## When it fires

The user taps the Bitcoin, USDT or Pix section header in the donation bottom sheet, expanding or collapsing it.

## Trigger source

`feature/donation/.../presentation/DonationViewModel.kt` — `DonationUiEvent.ToggleBitcoin`, `DonationUiEvent.ToggleUsdt`, `DonationUiEvent.TogglePix`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `section` | string | `bitcoin` | `bitcoin` \| `usdt` \| `pix` |
| `is_expanded` | boolean | `true` | New state after the toggle |

## Notes

- Param is `section`, not the planning doc's `method`: sections are coarser than the `method` values of [donation_method_copied](donation_method_copied.md) (the Bitcoin section contains both `btc_onchain` and `btc_lightning`; USDT contains both networks), so reusing `method` would mix two value sets in one dimension.
- One event with `is_expanded`, per the toggle convention in the README — never separate expanded/collapsed events.
