# chapter_listening_player_closed

**Tier:** P2 | **Domain:** Listening

The expanded player was closed.

## When it fires

Close button, swipe down or tap outside the player sheet.

## Trigger source

`feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `ChapterListeningPlayerUiEvent.OnDismiss`.

## Parameters

None.

## Notes

- Closing the sheet keeps listening; stopping is [chapter_listening_stopped](chapter_listening_stopped.md).
