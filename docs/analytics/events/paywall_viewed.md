# paywall_viewed

**Tier:** P1 | **Domain:** Monetization

Captures every paywall impression together with the surface that drove the user there. This is the top of the purchase funnel: conversion rate per entry point (`profile_menu` vs `day_study` vs `notes_limit` vs `chat`) tells which feature gate actually sells Pro.

## When it fires

The paywall screen is shown, regardless of which flow navigated to it — once per `PaywallViewModel` instance.

## Trigger source

`feature/paywall/.../presentation/viewmodel/PaywallViewModel.kt` — the `init` block, reading `PaywallNavRoute.source`.

The `source` is a required constructor parameter of `PaywallNavRoute`, so a new way into the paywall cannot be added without declaring where it came from — it is a compile error, not a silent gap. Current callers:

| `source` | Caller |
|---|---|
| `profile_menu` | `feature/profile/.../ProfileViewModel.kt` — `ProfileOptionItemType.BECOME_PRO` |
| `day_study` | `feature/day/.../DayViewModel.kt` — the locked AI-study card on the Day screen |
| `day_study_detail` | `feature/day_study/.../DayStudyRouteViewModel.kt` — the same card on the day-study detail pane |
| `notes_limit` | `feature/add_notes_free_warning/.../AddNotesFreeWarningUiActionCollector.kt` |
| `chat` | `feature/chat/.../ChatViewModel.kt` — the locked input bar after the free question quota runs out |

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `day_study` | Entry point: `PaywallEntrySource` in `core/model/.../route/`, lowercased |

## Notes

- Fires on the paywall being **shown**, not on the gate being clicked. Each gate logs its own click separately ([notes_limit_subscribe_clicked](notes_limit_subscribe_clicked.md), [ai_chat_subscribe_clicked](ai_chat_subscribe_clicked.md), [day_study_card_clicked](day_study_card_clicked.md) with `card_mode=locked`, [profile_option_clicked](profile_option_clicked.md)), so click-through and impression stay separable — a gap between the two means navigation was cancelled or deduplicated.
- Redundant with [screen_view](screen_view.md) (`screen_name=paywall`), which carries the same `source` via the route mapper. It is kept as a distinct event for funnel continuity with the data collected before the source became a route parameter.
- `day_study` and `day_study_detail` are the same locked card rendered by two different ViewModels. They are kept apart so the historical meaning of `day_study` (Day screen only) stays intact.
- Funnel: `paywall_viewed` → [paywall_plan_selected](paywall_plan_selected.md) → [purchase_started](purchase_started.md) → [purchase_completed](purchase_completed.md) / [purchase_failed](purchase_failed.md), all segmentable by `source`.
