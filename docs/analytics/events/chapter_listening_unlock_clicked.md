# chapter_listening_unlock_clicked

**Tier:** P1 | **Domain:** Listening

A free user asked to unlock the next chapter for listening.

## When it fires

Tap on Unlock in the mini player or in the expanded player after [chapter_listening_locked](chapter_listening_locked.md).

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `OnUnlockNextClick`; `feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnUnlockClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `mini_player` | Where the control was used: `mini_player` (the bar over the reader) \| `player` (the expanded player sheet) |

## Notes

- Goes on to the unlock sheet ([unlock_sheet_viewed](unlock_sheet_viewed.md) with `surface=chapter_listening`) or the Pro teaser when the daily unlock is used.
