# reading_suggestion_clicked

**Tier:** P2 | **Domain:** Reading

Captures using the previous/next chapter suggestions in the reader. Measures continuous in-app reading sessions (chapter chaining) vs one-chapter visits.

## When it fires

User taps the previous- or next-chapter suggestion at the bottom of the chapter reader.

## Trigger source

`feature/read/src/commonMain/kotlin/com/quare/bibleplanner/feature/read/presentation/ReadViewModel.kt` — `ReadUiEvent.OnNavigationSuggestionClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `direction` | string | `next` | `previous` \| `next` |
| `book_id` | string | `exodus` | Target book of the suggestion |
| `chapter_number` | int | `1` | Target chapter of the suggestion |

## Notes

- Discrepancy with the plan table: `ReadNavigationSuggestionModel` carries only `bookId` and `chapterNumber` — no direction. Direction is known only by which slot of `ReadNavigationSuggestionsModel` (`previous`/`next`) the tapped suggestion came from, so it must be passed from the UI layer or added to the model.
- Suggestions can cross book boundaries (last chapter of a book suggests the next book's chapter 1), so `book_id` may differ from the current screen's book.
- The navigation itself replaces the current entry, so the resulting [destination_view](destination_view.md) (`read`) still fires.
