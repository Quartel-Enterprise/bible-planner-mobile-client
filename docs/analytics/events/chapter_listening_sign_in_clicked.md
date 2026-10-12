# chapter_listening_sign_in_clicked

**Tier:** P2 | **Domain:** Listening

The person tapped Sign in after the player said the chapter needs a signed-in account.

## When it fires

Tap on the Sign in button the player shows when the chapter's audio was refused because the session ended (`not_signed_in` in [chapter_listening_audio_unavailable](chapter_listening_audio_unavailable.md)).

## Trigger source

`feature/read/.../presentation/listening/ListeningPlaybackActions.kt` — `handleUnavailableAction`, from `ReadListeningUiEvent.OnUnavailableActionClick` and `ChapterListeningPlayerUiEvent.OnUnavailableActionClick` while the status is `NEEDS_SIGN_IN`.

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `surface` | string | `mini_player` | Where the button was: `mini_player` (the bar over the reader) \| `player` (the expanded player sheet) |

## Notes

- Starting to listen while signed out opens the login warning instead, before the player exists.
- Once the person signs in, the player loads the chapter again by itself.
