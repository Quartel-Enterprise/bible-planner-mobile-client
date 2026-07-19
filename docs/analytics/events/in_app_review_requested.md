# in_app_review_requested

**Tier:** P2 | **Domain:** Reading

Fires when the app asks the platform for a native in-app review at a satisfying reading moment and the request is actually launched. Measures how often the gated review prompt reaches users and which achievement triggered it.

## When it fires

Right after a reading achievement crosses a milestone and the review policy allows a prompt: the app calls the platform in-app review API and the flow launches. Gated so it only ever fires once the post-install grace period has elapsed, once per app version, and outside the cross-version cooldown.

## Trigger source

`core/review/src/commonMain/kotlin/com/quare/bibleplanner/core/review/domain/usecase/impl/RequestReviewIfNeededUseCase.kt`, invoked from the reading achievement call sites: `ReadingPlanViewModel` (streak and Bible-progress milestone crossings) and `BookDetailsViewModel` (a book reaching 100% read).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `trigger` | string | `streak_milestone` | Which achievement surfaced the prompt: `streak_milestone` \| `progress_milestone` \| `book_completed` |

## Notes

- Only fires when the native review flow actually launches; if the platform cannot show it (no in-app review on iOS/Desktop, no activity, or an API error on Android) nothing is recorded and the prompt can be retried at the next milestone.
- This is an automatic, contextual prompt — not the explicit "Rate the app" settings row. That row always opens the store listing and is tracked only by [more_option_clicked](more_option_clicked.md) with `option=rate_app`.
- Reaching the review flow does **not** mean the user left a review — the OS review APIs never report whether the dialog was shown or acted upon.
- The achievements that trigger it are themselves tracked by [reading_streak_milestone](reading_streak_milestone.md), [bible_progress_milestone](bible_progress_milestone.md) and [book_completed](book_completed.md).
