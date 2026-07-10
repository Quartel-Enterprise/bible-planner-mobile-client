# github_sponsors_opened

**Tier:** P1 | **Domain:** Monetization

Captures the user leaving the donation sheet for the GitHub Sponsors page — the only recurring-donation channel. Since the sponsorship itself happens on github.com, this outbound click is the last observable step.

## When it fires

The user taps the GitHub Sponsors option in the donation bottom sheet, opening `https://github.com/sponsors/Quartel-Enterprise` in the browser.

## Trigger source

`feature/donation/.../presentation/DonationViewModel.kt` — `DonationUiEvent.OpenGitHubSponsors`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Donation sheet impressions are covered by [destination_view](destination_view.md) (`destination_name=donation`); this event divided by those impressions gives the sponsors click-through rate.
- Related: [donation_method_copied](donation_method_copied.md) for the one-off crypto/Pix channels.
