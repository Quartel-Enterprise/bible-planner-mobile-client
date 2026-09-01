# day_study_card_clicked

**Tier:** P1 | **Domain:** DayStudy

Captures every tap on the AI study entry card, split by the card's mode and by which surface rendered it. This is the entry point of the AI-study funnel and — via the `locked` mode — a paywall driver.

## When it fires

The user taps the AI study card, in any of its modes, on either of the two surfaces that render it.

## Trigger source

- `feature/day_study/.../presentation/viewmodel/DayStudyViewModel.kt` — `DayStudyUiEvent.OnCardClick` (`source=day_screen`)
- `feature/day_study/.../presentation/viewmodel/DayStudyRouteViewModel.kt` — `DayStudyRouteUiEvent.OnCardClick` (`source=day_study_detail`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `card_mode` | string | `generate` | `view` \| `generate` \| `locked` (snake_case of `DayStudyCardMode`) |
| `is_pro` | boolean | `false` | Whether the user has the Pro entitlement |
| `source` | string | `day_screen` | Which surface rendered the card: `day_screen` \| `day_study_detail` |

## Notes

- What follows depends on the mode: `locked` → paywall ([paywall_viewed](paywall_viewed.md) with `source=day_study` or `day_study_detail`, matching this event's `source`); `generate` → login check then generation, navigating to the day-study route either way; `view` → navigate to the day-study route, which opens the cached study or regenerates.
- If a generation is already in flight, the tap just navigates to the day-study route (early return before the mode branch) — still log the click with the current `card_mode`.
- `generate` taps by anonymous users route to the login warning (covered by [screen_view](screen_view.md) with `reason=day_study`) without starting a generation.
- `source` was added when the detail pane started logging this event; before that, the detail pane's taps were not logged at all, so rows with no `source` are all `day_screen`.
- Funnel: `day_study_card_clicked` → [day_study_generation_started](day_study_generation_started.md) → [day_study_generation_completed](day_study_generation_completed.md) / [day_study_generation_failed](day_study_generation_failed.md) → [day_study_opened](day_study_opened.md).
