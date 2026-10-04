# chapter_listening_auto_next_toggled

**Tier:** P2 | **Domain:** Listening

Continuing to the next chapter automatically was turned on or off.

## When it fires

Tap on the auto next switch of the expanded player (disabled during the reading of today).

## Trigger source

`feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnAutoNextToggle`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | string | `"false"` | The state the setting was moved to |
