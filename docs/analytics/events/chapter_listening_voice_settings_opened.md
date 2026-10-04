# chapter_listening_voice_settings_opened

**Tier:** P2 | **Domain:** Listening

The device voice settings were opened from the player.

## When it fires

Tap on Settings in the mini player when no voice is installed, or on the settings link of the voice list.

## Trigger source

`feature/read/.../presentation/listening/ReadListeningViewModel.kt` — `OnVoiceSettingsClick`; `feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnVoiceSettingsClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `mini_player` | Where the control was used: `mini_player` (the bar over the reader) \| `player` (the expanded player sheet) |

## Notes

- Android opens the text-to-speech settings; iOS can only open the app's page in Settings.
