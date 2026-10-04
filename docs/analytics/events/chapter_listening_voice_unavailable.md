# chapter_listening_voice_unavailable

**Tier:** P2 | **Domain:** Listening

Listening was asked for but the device has no voice for the language of the selected Bible.

## When it fires

A chapter opens in the player and the system text-to-speech engine lists no installed voice for that language.

## Trigger source

`core/chapter_listening/.../domain/controller/ChapterListeningControllerImpl.kt` — `trackOpenedSegment`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `language` | string | `pt-BR` | Language tag the Bible is read in |

## Notes

- The mini player then offers the device voice settings ([chapter_listening_voice_settings_opened](chapter_listening_voice_settings_opened.md)).
