# paywall_plan_selected

**Tier:** P1 | **Domain:** Monetization

Captures which subscription plan card the user taps on the paywall. Shows plan preference (monthly vs annual) before any purchase intent, and how often users switch between plans while deciding.

## When it fires

The user taps a plan card on the paywall, changing the selected plan.

## Trigger source

`feature/paywall/.../viewmodel/PaywallViewModel.kt` — `PaywallUiEvent.OnPlanSelected(planType)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `subscription_plan` | string | `annual` | Selected plan: `monthly` \| `annual` (from `SubscriptionPlanType`) |

## Notes

- Named `subscription_plan` instead of the planning doc's `plan_type` because the standard dictionary already reserves `plan_type` for the reading plan (`chronological` \| `books`); reusing it with `monthly`/`annual` values would corrupt that dimension.
- Can fire multiple times per paywall visit (user toggling between plans); the last one before [purchase_started](purchase_started.md) is the plan actually purchased.
- A default plan is pre-selected when the paywall opens; that initial selection is not a tap and is not logged.
