# chapter_listening_sleep_timer_ended

**Tier:** P2 | **Domain:** Listening

The sleep timer stopped the reading.

## When it fires

A 15 or 30 minute timer ran out while reading, or the end of chapter timer reached the end of the chapter.

## Trigger source

`core/chapter_listening/.../domain/controller/ListeningSleepTimer.kt` — `end`, called when the countdown runs out (`ListeningPlayback`) and when the chapter ends under the end-of-chapter option (`ChapterListeningControllerImpl.onSegmentFinished`).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `option` | string | `countdown` | `countdown` (a 15 or 30 minute timer) \| `end_of_chapter` |
