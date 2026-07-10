# book_favorite_toggled

**Tier:** P1 | **Domain:** Reading

Captures favoriting/unfavoriting a book. Favorites reveal which books users care about and whether the favorites filter feature earns its place.

## When it fires

User taps the favorite (heart) toggle on a book card in the books list or on the book details screen.

## Trigger source

- `feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnToggleFavorite` (`source=books_list`)
- `feature/book_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/bookdetails/presentation/viewmodel/BookDetailsViewModel.kt` — `BookDetailsUiEvent.OnToggleFavorite` (`source=book_details`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `psalms` | Book being (un)favorited |
| `is_favorite` | boolean | `true` | New favorite status after the toggle |
| `source` | string | `books_list` \| `book_details` | Which surface triggered the toggle |

## Notes

- One event with `is_favorite`, never separate favorited/unfavorited events (see README naming conventions).
- Usage of the favorites filter itself is tracked by [books_filter_toggled](books_filter_toggled.md).
