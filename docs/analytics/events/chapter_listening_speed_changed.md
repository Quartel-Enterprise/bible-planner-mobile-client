# chapter_listening_speed_changed

**Tier:** P2 | **Domain:** Listening

The reading speed was changed.

## When it fires

Tap on a speed chip of the expanded player.

## Trigger source

`feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnSpeedClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `speed` | double | `1.25` | The chosen speed: `0.75` \| `1` \| `1.25` \| `1.5` \| `2` |

## Notes

- The speed is saved on the device and reused next time.
