# chapter_listening_voice_previewed

**Tier:** P2 | **Domain:** Listening

A voice was previewed reading the current verse.

## When it fires

Tap on Listen next to a voice.

## Trigger source

`feature/read/.../presentation/listening/player/ChapterListeningPlayerViewModel.kt` — `OnVoicePreviewClick`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enhanced` | string | `"false"` | Whether the previewed voice is an enhanced/high quality one |
