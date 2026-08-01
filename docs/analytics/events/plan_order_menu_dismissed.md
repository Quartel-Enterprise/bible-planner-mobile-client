# plan_order_menu_dismissed

**Tier:** P2 | **Domain:** Books

Captures the user closing the reading order menu without picking an order. Paired with [plan_order_menu_clicked](plan_order_menu_clicked.md), it shows how often the menu is opened out of curiosity rather than to switch order.

## When it fires

User dismisses the order menu on the reading plan screen by tapping outside it or pressing back.

## Trigger source

`feature/reading_plan/.../ReadingPlanViewModel.kt` — `ReadingPlanUiEvent.OnOrderMenuDismiss`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Does not fire when the menu closes because an order was picked; that path fires [plan_selected](plan_selected.md).
