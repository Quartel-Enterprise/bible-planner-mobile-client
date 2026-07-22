# day_study_retry_clicked

**Tier:** P2 | **Domain:** DayStudy

Captures the user retrying a failed AI study generation from the day-study screen's error state. Together with [day_study_generation_failed](day_study_generation_failed.md), it shows how many failures users actually try to recover from.

## When it fires

The user taps "Try again" on the generation-error state of the day-study screen (shown when a generation fails for any reason other than the free-quota limit).

## Trigger source

`feature/day_study/.../presentation/model/DayStudyRouteUiEvent.kt` — `OnRetryClick`, tracked automatically (no parameters). The retry then re-runs the normal generation flow, so `day_study_generation_started`/`completed`/`failed` follow as usual.

## Parameters

None.

## Notes

- Only exists on the day-study route screen; on the day screen's card, retrying is just clicking Generate again ([day_study_card_clicked](day_study_card_clicked.md)).
