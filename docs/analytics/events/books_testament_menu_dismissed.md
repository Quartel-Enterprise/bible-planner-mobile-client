# books_testament_menu_dismissed

**Tier:** P2 | **Domain:** Books

Captures the testament selector menu on the Books tab being dismissed. Compared against [books_testament_menu_opened](books_testament_menu_opened.md), it shows how often the menu is opened without switching testament.

## When it fires

The testament dropdown's `onDismissRequest` fires — Compose's generic dismiss callback (tap-outside or back press). Picking an option closes the menu through [testament_switched](testament_switched.md).

## Trigger source

`feature/books/.../BooksViewModel.kt` — `BooksUiEvent.OnDismissTestamentMenu`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Fires from a generic dismiss callback, so treat volume trends directionally, not as a strict funnel step.
