# paywall_dismissed

**Tier:** P2 | **Domain:** Monetization

Captures the user leaving the paywall without buying. Together with the plan selected at that moment it shows how far into consideration users get before bouncing.

## When it fires

The user taps the back/close control on the paywall.

## Trigger source

`feature/paywall/.../viewmodel/PaywallViewModel.kt` — `PaywallUiEvent.OnBackClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `subscription_plan` | string | `monthly` | Plan selected at dismissal time: `monthly` \| `annual` |

## Notes

- The ViewModel also auto-navigates back when the Pro entitlement arrives from elsewhere (`observeProStatus` → `NavigateBack` when `purchaseInitiated` is false, e.g. a promotional entitlement granted server-side). That programmatic exit is **not** a dismissal and must not be logged — only the explicit `OnBackClick` path.
- Successful purchases leave the paywall via the Congrats screen, so they never produce this event.
- Funnel context: [paywall_viewed](paywall_viewed.md) minus ([purchase_completed](purchase_completed.md) + `paywall_dismissed`) ≈ store-sheet abandonment; see [purchase_failed](purchase_failed.md) `user_cancelled`.
