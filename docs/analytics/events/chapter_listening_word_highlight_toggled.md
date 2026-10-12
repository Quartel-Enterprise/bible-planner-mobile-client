# chapter_listening_word_highlight_toggled

**Tier:** P2 | **Domain:** Listening

Highlighting the word being read, in the player and in the reader, was turned on or off.

## When it fires

Tap on the word highlight switch of the expanded player.

## Trigger source

`feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnWordHighlightToggle`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | string | `"false"` | The state the setting was moved to |
