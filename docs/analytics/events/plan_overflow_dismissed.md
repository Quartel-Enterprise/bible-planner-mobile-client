# plan_overflow_dismissed

**Tier:** P2 | **Domain:** Books

Captures the overflow menu on the reading plan screen closing, whether by tapping an option, tapping outside, or pressing back. Compared against [plan_overflow_clicked](plan_overflow_clicked.md), it shows how often the menu is opened without selecting an option.

## When it fires

The overflow dropdown's `onDismissRequest` fires — this is Compose's generic dismiss callback, so it does not distinguish the exact cause.

## Trigger source

`feature/reading_plan/.../ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnOverflowDismiss`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Because this fires from a generic dismiss callback, it is not a precise "user declined the menu" signal — treat volume trends directionally, not as a strict funnel step.
