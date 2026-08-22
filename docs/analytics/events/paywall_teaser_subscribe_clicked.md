# paywall_teaser_subscribe_clicked

**Tier:** P1 | **Domain:** Monetization

Captures the user accepting the upsell on the generic "why Pro" teaser sheet shown before the real paywall. It is the click half of that gate: whichever `reason` shows the teaser, this measures how many of the people who saw it acted on it.

## When it fires

The user taps "Subscribe to Pro" on the paywall teaser sheet.

## Trigger source

`feature/paywall_teaser/.../presentation/model/PaywallTeaserUiEvent.kt` — `PaywallTeaserUiEvent.OnSubscribeClick`, tracked from `PaywallTeaserViewModel.handleEvent`.

The teaser is reused by every feature that gates a capability behind Pro; `reason` is a required constructor parameter of `PaywallTeaserNavRoute` (`core/model/.../route/`), so a new gate cannot reuse the sheet without declaring its reason.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `highlight_custom_color` | Why the teaser was shown: `PaywallTeaserReason` in `core/model/.../route/`, lowercased |

## Notes

- Pairs with [paywall_teaser_dismissed](paywall_teaser_dismissed.md), the "use the free option" half of the same sheet.
- Navigates to the real paywall replacing the teaser on the stack, so the resulting impression is [paywall_viewed](paywall_viewed.md) with a matching `source`.
- Funnel: [highlight_custom_color_locked_clicked](highlight_custom_color_locked_clicked.md) → `paywall_teaser_subscribe_clicked` → [paywall_viewed](paywall_viewed.md) → [purchase_completed](purchase_completed.md). Future gates that reuse the teaser slot into the same funnel shape.
