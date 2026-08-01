# plan_order_menu_clicked

**Tier:** P2 | **Domain:** Books

Captures the user opening the reading order menu from the compact chip on the reading plan screen. Shows how often the collapsed selector is used to switch order, compared to the segmented buttons on wider layouts.

## When it fires

User taps the compact order chip on the reading plan screen. It only exists when the header is too narrow for the segmented buttons.

## Trigger source

`feature/reading_plan/.../ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnOrderMenuClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Closing the menu without picking an order is tracked separately: [plan_order_menu_dismissed](plan_order_menu_dismissed.md).
- Picking an order fires [plan_selected](plan_selected.md), the same event the segmented buttons fire.
