# chapter_listening_player_opened

**Tier:** P2 | **Domain:** Listening

The expanded player was opened from the mini player.

## When it fires

Tap on the title of the mini player.

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `ReadListeningUiEvent.OnMiniPlayerClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `mini_player` | Always `mini_player`; opening it from an active entry point is [chapter_listening_entry_clicked](chapter_listening_entry_clicked.md) with `is_active=true` |

## Notes

- The matching impression is [screen_view](screen_view.md) with `screen_name=chapter_listening_player`.
