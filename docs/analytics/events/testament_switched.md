# testament_switched

**Tier:** P2 | **Domain:** Books

Captures the user switching between Old and New Testament on the Books tab. Shows how browsing splits between the testaments and whether the persisted selection matches actual usage.

## When it fires

User taps the Old/New Testament selector on the Books tab.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnTestamentSelect(testament: BookTestament)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `testament` | string | `new` | Selected `BookTestament` value mapped to `old` \| `new` (from `OldTestament` / `NewTestament`) |

## Notes

- The selection is persisted, so the event only fires on explicit switches — the restored value at screen entry does not fire.
- Related: [books_search_used](books_search_used.md), [books_filter_toggled](books_filter_toggled.md).
