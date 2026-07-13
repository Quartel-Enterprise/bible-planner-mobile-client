# plan_overflow_clicked

**Tier:** P2 | **Domain:** Books

Captures the user opening the overflow menu on the reading plan screen. Contextualizes how often the menu is discovered before [plan_overflow_option_clicked](plan_overflow_option_clicked.md) fires.

## When it fires

User taps the overflow (⋮) icon on the reading plan screen.

## Trigger source

`feature/reading_plan/.../ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnOverflowClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Closing the menu without selecting an option is tracked separately: [plan_overflow_dismissed](plan_overflow_dismissed.md).
