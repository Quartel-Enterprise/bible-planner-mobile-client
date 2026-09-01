# day_reading_complete_cta_clicked

**Tier:** P1 | **Domain:** DayReadingComplete

Captures the tap on the sheet's single call to action, whose behavior branches on `account_state`:
generate the study, go straight to viewing it (Pro), or open the paywall.

## When it fires

The user taps the sheet's main button — "Gerar estudo do dia"/"...desse dia" (free with quota), "Ver
estudo do dia"/"...desse dia" (Pro), or "Assinar o Pro" (free, quota exhausted).

## Trigger source

`feature/day_reading_complete/.../presentation/viewmodel/DayReadingCompleteViewModel.kt` —
`DayReadingCompleteUiEvent.OnCtaClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `account_state` | string | `free` | `free` \| `free_exhausted` \| `pro`, same as [day_reading_complete_shown](day_reading_complete_shown.md) |
| `source` | string | `day_reading_complete` | Constant; mirrors `day_study_card_clicked`'s `source` so the two entry points can be compared |

## Notes

- `free` and `pro` both call `DayStudyGenerationCoordinator.start` (a no-op when the study is already
  unlocked for the day) and navigate to the day-study route — only the button copy differs. `free_exhausted`
  navigates to the paywall instead ([paywall_viewed](paywall_viewed.md) with `source=day_study`).
- Funnel continues at [day_study_generation_started](day_study_generation_started.md) for the
  `free`/`pro` branches.
