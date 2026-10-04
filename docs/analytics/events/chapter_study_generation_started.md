# chapter_study_generation_started

**Tier:** P1 | **Domain:** ChapterStudy

Captures the start of an AI chapter-study generation — the moment backend cost is incurred. Volume by `is_pro` shows how much of the usage is free quota being spent.

## When it fires

The study screen opens for a chapter with no study on the device and no generation already running, the device is online, and `ChapterStudyGenerationCoordinator.start` is invoked. Also fires when the reader retries after a failure.

## Trigger source

`feature/chapter_study/.../presentation/viewmodel/ChapterStudyViewModel.kt` — `startGeneration()`, reached from `openStudy()` on entering the screen from `ChapterStudyUiEvent.OnRetryClick` and, with `is_rewarded=true`, after a rewarded video ([rewarded_ad_earned](rewarded_ad_earned.md)) or from the locked hero when an earned reward is still unserved.

`feature/read/.../presentation/ReadViewModel.kt` — `openRewardedChapterStudy()`: a reward earned on the unlock sheet opened from the reader (`is_pro=false`, `is_rewarded=true`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |
| `is_pro` | string | `"false"` | Whether the user has the Pro entitlement |
| `is_rewarded` | string | `"false"` | Whether this generation was unlocked with a rewarded video (the app sent `reward: true`) |

## Notes

- Not fired when the study is already on the device (see [chapter_study_opened](chapter_study_opened.md) with `is_cached=true`) or when the device is offline (see [chapter_study_generation_failed](chapter_study_generation_failed.md) `reason=offline`).
- Generation is app-scoped (`ChapterStudyGenerationCoordinator`), so it keeps running if the reader leaves the screen; reopening the screen follows the running generation without logging a second start.
