# chapter_listening_stopped

**Tier:** P2 | **Domain:** Listening

The person stopped listening.

## When it fires

Tap on the close button of the mini player.

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `ReadListeningUiEvent.OnCloseClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `mini_player` | Where the control was used: `mini_player` (the bar over the reader) \| `player` (the expanded player sheet) |

## Notes

- Listening also stops when the last reader screen closes; that stop is not tracked.
