# chapter_study_generation_failed

**Tier:** P1 | **Domain:** ChapterStudy

Captures AI chapter-study generations that fail or are blocked before starting, split by reason. `limit_reached` measures free-quota pressure; `offline` and `error` measure feature availability.

## When it fires

- `limit_reached` — the server refuses the generation because the free quota is exhausted (the study screen is then replaced by the Pro teaser).
- `offline` — the device is offline when the screen tries to start the generation, or goes offline while it is running.
- `error` — any other failure of a running generation.

## Trigger source

- `core/chapter_study/.../domain/coordinator/ChapterStudyGenerationCoordinatorImpl.kt` — `failGeneration(...)` (`limit_reached` / `error` / `offline` during a generation)
- `feature/chapter_study/.../presentation/viewmodel/ChapterStudyViewModel.kt` — `startGeneration()`, the `!isConnected()` branch (`offline` before starting)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `book_id` | string | `GEN` | Book of the chapter being studied (`BookId` name) |
| `chapter_number` | int | `3` | 1-based chapter within the book |
| `reason` | string | `limit_reached` | `limit_reached` \| `offline` \| `error` |
| `is_pro` | string | `"false"` | Whether the user has the Pro entitlement |
| `is_rewarded` | string | `"false"` | Whether this generation was unlocked with a rewarded video (the app sent `reward: true`). Absent when the ViewModel rejects the start before it reaches the coordinator (offline pre-check) |
| `duration_ms` | int | `1200` | Time the generation ran before failing; absent when it was blocked before starting |

## Notes

- `limit_reached` here is the rare race: the reader's entry tap normally sends a free user without quota straight to the teaser, without ever opening the study screen (see [chapter_study_entry_clicked](chapter_study_entry_clicked.md)).
