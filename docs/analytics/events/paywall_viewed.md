# paywall_viewed

**Tier:** P1 | **Domain:** Monetization

Captures every paywall impression together with the surface that drove the user there. This is the top of the purchase funnel: conversion rate per entry point (`more_menu` vs `day_study` vs `notes_limit`) tells which feature gate actually sells Pro.

## When it fires

The paywall screen is shown, regardless of which flow navigated to it.

## Trigger source

`PaywallNavRoute` carries no arguments today, so the `source` cannot be read on the paywall side. Either add a `source` param to the route or log the event caller-side at each navigation point:

- `feature/more/.../viewmodel/MoreViewModel.kt` — `MoreUiEvent.OnItemClick` with `MoreOptionItemType.BECOME_PRO` (`source=more_menu`)
- `feature/day/.../viewmodel/DayViewModel.kt` — `DayUiEvent.OnDayStudySubscribeClick` → `navigateToPaywall()` (`source=day_study`; originates from `DayStudyUiAction.NavigateToPaywall` when the locked card is clicked)
- `feature/add_notes_free_warning/.../utils/AddNotesFreeWarningUiActionCollector.kt` — `AddNotesFreeWarningUiAction.NavigateToPro` (`source=notes_limit`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `day_study` | Entry point: `more_menu` \| `day_study` \| `notes_limit` |

## Notes

- The bare paywall impression is also covered by [destination_view](destination_view.md) (`destination_name=paywall`); this event exists to carry `source`, which `destination_view` cannot know.
- Funnel: `paywall_viewed` → [paywall_plan_selected](paywall_plan_selected.md) → [purchase_started](purchase_started.md) → [purchase_completed](purchase_completed.md) / [purchase_failed](purchase_failed.md), all segmentable by `source` once it is a route param.
- If a new caller of `PaywallNavRoute` is added, extend the `source` enum here.
