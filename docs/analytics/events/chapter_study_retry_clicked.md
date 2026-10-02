# chapter_study_retry_clicked

**Tier:** P2 | **Domain:** ChapterStudy

Captures the reader retrying a failed chapter-study generation. Together with [chapter_study_generation_failed](chapter_study_generation_failed.md), it shows how many failures readers try to recover from.

## When it fires

The reader taps "Try again" on the error state of the study screen.

## Trigger source

`feature/chapter_study/.../presentation/viewmodel/ChapterStudyViewModel.kt` — `ChapterStudyUiEvent.OnRetryClick` → `onRetryClick()`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |

## Notes

- The retry re-runs the normal start, so [chapter_study_generation_started](chapter_study_generation_started.md) or a `reason=offline` failure follows.
