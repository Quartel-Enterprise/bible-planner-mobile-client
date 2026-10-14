# books_testament_menu_opened

**Tier:** P2 | **Domain:** Books

Captures the user opening the testament selector on the Books tab. Menu open rate contextualizes how often the selector is discovered before [testament_switched](testament_switched.md) fires.

## When it fires

User taps the testament chip (All / Old Testament / New Testament) on the Books tab, opening its dropdown menu.

## Trigger source

`feature/books/.../BooksViewModel.kt` — `BooksUiEvent.OnToggleTestamentMenu`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Closing the menu (via tap-outside or back) is tracked separately: [books_testament_menu_dismissed](books_testament_menu_dismissed.md).
