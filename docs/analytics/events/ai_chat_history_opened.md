# ai_chat_history_opened

**Tier:** P2 | **Domain:** AiChat

Opens of the conversation history drawer, with how many conversations the user has — a proxy for chat retention.

## When it fires

The user taps the history icon in the chat app bar.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnHistoryClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `count` | int | `7` | Conversations the user currently has |

## Notes

- The list is refreshed from the server on open, so `count` is the value known at tap time (before the refresh lands).
