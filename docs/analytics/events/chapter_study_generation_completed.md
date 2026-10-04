# chapter_study_generation_completed

**Tier:** P1 | **Domain:** ChapterStudy

Captures an AI chapter-study generation finishing successfully, with how long it took. Against [chapter_study_generation_started](chapter_study_generation_started.md) it gives the success rate of the feature.

## When it fires

The generation stream delivers the finished study and it has been stored on the device.

## Trigger source

`core/chapter_study/.../domain/coordinator/ChapterStudyGenerationCoordinatorImpl.kt` — `collectGeneration(...)`, on the `Completed` event.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |
| `is_pro` | string | `"false"` | Whether the user has the Pro entitlement |
| `is_rewarded` | string | `"false"` | Whether this generation was unlocked with a rewarded video (the app sent `reward: true`) |
| `duration_ms` | int | `5600` | Time from the start of the generation to the finished study |

## Notes

- Fired by the app-scoped coordinator, so it is logged even when the reader has left the study screen.
- A study the server already had cached completes in well under a second; `duration_ms` separates those from fresh generations.
