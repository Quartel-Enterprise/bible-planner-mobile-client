# chapter_study_opened

**Tier:** P2 | **Domain:** ChapterStudy

Captures a chapter study actually being shown with content — the consumption side of the feature. The cached share shows how much value each generated study delivers beyond its first read.

## When it fires

The study screen shows a study: one already on the device when the screen opens, or a generation finishing while the screen is open.

## Trigger source

`feature/chapter_study/.../presentation/viewmodel/ChapterStudyViewModel.kt` — `showStudy(...)`, reached from `openStudy()` (`is_cached=true`) and `onGenerationDone(...)` (`is_cached=false`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |
| `is_cached` | string | `"true"` | `true` when the study was already on the device; `false` when a generation just produced it |

## Notes

- A generation that finished while the screen was closed counts as `is_cached=false` the first time the reader comes back to it.
