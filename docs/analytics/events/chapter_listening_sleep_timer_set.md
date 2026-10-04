# chapter_listening_sleep_timer_set

**Tier:** P2 | **Domain:** Listening

A sleep timer option was chosen.

## When it fires

Tap on a sleep timer chip of the expanded player.

## Trigger source

`feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnSleepTimerClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `option` | string | `thirty_minutes` | `off` \| `fifteen_minutes` \| `thirty_minutes` \| `end_of_chapter` |

## Notes

- The end of the timer is [chapter_listening_sleep_timer_ended](chapter_listening_sleep_timer_ended.md).
