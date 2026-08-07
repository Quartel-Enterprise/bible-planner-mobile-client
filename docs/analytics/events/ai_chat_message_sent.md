# ai_chat_message_sent

**Tier:** P0 | **Domain:** AiChat

The core usage metric of the chat: one event per question the user sends, whether typed or picked from a suggestion.

## When it fires

The user taps send with non-empty text, or taps a suggestion chip. Fires when the request leaves the client, before the answer streams back.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnSendClick`, `ChatUiEvent.OnSuggestionClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `has_context` | boolean | `true` | Whether the conversation carries a reading context |
| `is_new_conversation` | boolean | `false` | Whether this question creates the conversation |

## Notes

- A question blocked by the rate-limit cooldown, by the locked input, or by the reader not being signed in never reaches this event — the send is refused and, when signed out, the login warning opens instead.
- Failures after this point are covered by [ai_chat_answer_failed](ai_chat_answer_failed.md); quota is only consumed on success.
