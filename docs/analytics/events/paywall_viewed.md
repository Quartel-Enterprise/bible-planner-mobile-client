# paywall_viewed

**Tier:** P1 | **Domain:** Monetization

Captures every paywall entry together with the surface that drove the user there. This is the top of the purchase funnel: conversion rate per entry point (`profile_menu` vs `day_study` vs `notes_limit` vs `chat`) tells which feature gate actually sells Pro.

## When it fires

The user triggers navigation to the paywall, from any of the surfaces that gate a Pro feature.

## Trigger source

`PaywallNavRoute` carries no arguments, so the `source` cannot be read on the paywall side — the event is logged caller-side at each navigation point. Every call site of `PaywallNavRoute` must emit it:

- `feature/profile/.../viewmodel/ProfileViewModel.kt` — `ProfileUiEvent.OnItemClick` with `ProfileOptionItemType.BECOME_PRO` (`source=profile_menu`)
- `feature/day/.../model/DayUiEvent.kt` — `DayUiEvent.OnDayStudySubscribeClick` (`source=day_study`; originates from `DayStudyUiAction.NavigateToPaywall` when the locked AI-study card on the Day screen is clicked)
- `feature/day_study/.../viewmodel/DayStudyRouteViewModel.kt` — `onCardClick()` with `DayStudyCardMode.LOCKED` (`source=day_study_detail`; the same locked card on the day-study detail pane)
- `feature/add_notes_free_warning/.../model/AddNotesFreeWarningUiEvent.kt` — `AddNotesFreeWarningUiEvent.OnSubscribeToPro` (`source=notes_limit`)
- `feature/chat/.../viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnSubscribeClick` (`source=chat`; the locked input bar shown once the free question quota runs out)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `day_study` | Entry point: `profile_menu` \| `day_study` \| `day_study_detail` \| `notes_limit` \| `chat` |

## Notes

- The bare paywall impression is also covered by [screen_view](screen_view.md) (`screen_name=paywall`); this event exists to carry `source`, which `screen_view` cannot know.
- `day_study` and `day_study_detail` are the same locked card rendered by two different ViewModels — the card embedded in the Day screen and the one on the day-study detail pane. They are kept apart so the historical meaning of `day_study` (Day screen only) stays intact.
- Fired alongside the surface's own click event where one exists: [ai_chat_subscribe_clicked](ai_chat_subscribe_clicked.md) for `chat`, [profile_option_clicked](profile_option_clicked.md) for `profile_menu`.
- Funnel: `paywall_viewed` → [paywall_plan_selected](paywall_plan_selected.md) → [purchase_started](purchase_started.md) → [purchase_completed](purchase_completed.md) / [purchase_failed](purchase_failed.md), all segmentable by `source`.
- If a new caller of `PaywallNavRoute` is added, it must log this event and the `source` enum above must be extended.
