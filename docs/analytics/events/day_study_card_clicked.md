# day_study_card_clicked

**Tier:** P1 | **Domain:** DayStudy

Captures every tap on the AI study entry card on the day screen, split by the card's mode. This is the entry point of the AI-study funnel and — via the `locked` mode — a paywall driver.

## When it fires

The user taps the AI study card on the day screen, in any of its modes.

## Trigger source

`feature/day_study/.../presentation/viewmodel/DayStudyViewModel.kt` — `DayStudyUiEvent.OnCardClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `card_mode` | string | `generate` | `view` \| `generate` \| `locked` (snake_case of `DayStudyCardMode`) |
| `is_pro` | boolean | `false` | Whether the user has the Pro entitlement |

## Notes

- What follows depends on the mode: `locked` → paywall ([paywall_viewed](paywall_viewed.md) with `source=day_study`); `generate` → login check then generation or cached open; `view` → open cached study or regenerate.
- If a study is already open in memory or a generation is in flight, the tap just reopens the sheet (early return before the mode branch) — still log the click with the current `card_mode`.
- `generate` taps by anonymous users route to the login warning (covered by [destination_view](destination_view.md) with `reason=day_study`) without starting a generation.
- Funnel: `day_study_card_clicked` → [day_study_generation_started](day_study_generation_started.md) → [day_study_generation_completed](day_study_generation_completed.md) / [day_study_generation_failed](day_study_generation_failed.md) → [day_study_opened](day_study_opened.md).
