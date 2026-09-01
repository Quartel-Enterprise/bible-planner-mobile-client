# paywall_teaser_dismissed

**Tier:** P2 | **Domain:** Monetization

Captures the user declining the upsell on the generic "why Pro" teaser sheet — closing it, tapping the free-option link, or tapping outside — without reaching the real paywall.

## When it fires

The user dismisses the paywall teaser sheet by any of its close affordances.

## Trigger source

`feature/paywall_teaser/.../presentation/model/PaywallTeaserUiEvent.kt` — `PaywallTeaserUiEvent.OnDismiss`, tracked from `PaywallTeaserViewModel.handleEvent`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `highlight_custom_color` | Why the teaser was shown: `PaywallTeaserReason` in `core/model/.../route/`, lowercased |

## Notes

- Pairs with [paywall_teaser_subscribe_clicked](paywall_teaser_subscribe_clicked.md), the "subscribe" half of the same sheet.
- A gap between a lock-tap event (e.g. [highlight_custom_color_locked_clicked](highlight_custom_color_locked_clicked.md)) and either this event or `paywall_teaser_subscribe_clicked` means the user backed out without an explicit choice — the system back gesture and closing the whole reader both do this, since the teaser sheet does not distinguish them.
