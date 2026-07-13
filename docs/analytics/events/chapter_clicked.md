# chapter_clicked

**Tier:** P2 | **Domain:** Reading

Captures the user tapping a chapter to open it on the Read screen. Because this click also triggers navigation (which `destination_view` logs separately), this event isolates click-through intent — which chapters get tapped, from which surface — from the resulting screen-view. Fired from two different surfaces.

## When it fires

User taps a chapter row on the book-details screen, or a chapter/passage entry on the Day screen.

## Trigger source

- `feature/book_details/src/commonMain/kotlin/com/quare/bibleplanner/feature/bookdetails/presentation/model/BookDetailsUiModels.kt` — `BookDetailsUiEvent.OnChapterClick(chapterNumber: Int)`
- `feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/model/DayUiEvent.kt` — `DayUiEvent.OnChapterClick(strategy: ChapterClickStrategy)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `chapter_number` | int | `5` | Standard param; only available from `BookDetailsUiEvent.OnChapterClick`, which carries the raw chapter number. Not present when fired from the Day screen, since `ChapterClickStrategy` doesn't expose one. |
| `source` | string | `book_details` \| `day_screen` | Which screen triggered the click |

## Notes

- The Day-screen case only carries a `ChapterClickStrategy`, not a raw chapter number, so that trigger site omits `chapter_number` and only sends `source`.
- Related: [destination_view](destination_view.md) (`read` with `book_id`, `chapter_number`) fires separately once the destination becomes visible.
