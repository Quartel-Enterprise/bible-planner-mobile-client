# testament_switched

**Tier:** P2 | **Domain:** Books

Captures the user switching the testament filter on the Books tab (All, Old Testament or New Testament). Shows how browsing splits between the testaments and whether the persisted selection matches actual usage.

## When it fires

User picks an option in the testament chip's dropdown menu on the Books tab.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnTestamentSelect(testamentFilter: BookTestamentFilter)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `testament` | string | `new` | Selected `BookTestamentFilter` value mapped to `all` \| `old` \| `new` (from `All` / `OldTestament` / `NewTestament`) |

## Notes

- The selection is persisted, so the event only fires on explicit switches — the restored value at screen entry does not fire, and picking the option that is already selected only closes the menu.
- `all` was added when the selector became a dropdown chip; `All` is the default when nothing is persisted (it used to be `OldTestament`).
- Related: [books_search_used](books_search_used.md), [books_filter_toggled](books_filter_toggled.md).
