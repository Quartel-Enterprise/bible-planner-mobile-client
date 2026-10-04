# chapter_listening_control_clicked

**Tier:** P2 | **Domain:** Listening

A playback control of the mini player or the expanded player was used.

## When it fires

Play/pause, resume after an interruption, previous/next verse, previous/next chapter or a jump with the progress bar.

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `OnPlayPauseClick` (manual) and `OnNextVerseClick`; `feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnPlayPauseClick` (manual), `OnPreviousVerseClick`, `OnNextVerseClick`, `OnPreviousChapterClick`, `OnNextChapterClick`, `OnSeek`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `control` | string | `pause` | `play` \| `pause` \| `resume` \| `next_verse` \| `previous_verse` \| `next_chapter` \| `previous_chapter` \| `seek` |
| `surface` | string | `mini_player` | Where the control was used: `mini_player` (the bar over the reader) \| `player` (the expanded player sheet) |

## Notes

- Controls used from the notification, lock screen or headphones are not tracked.
