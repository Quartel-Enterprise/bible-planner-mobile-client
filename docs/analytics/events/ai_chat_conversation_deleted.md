# ai_chat_conversation_deleted

**Tier:** P2 | **Domain:** AiChat

Deletions of a conversation, confirmed through the inline strip in the history drawer.

## When it fires

The user taps "Delete" in the confirmation strip of a conversation row.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnDeleteConfirm`

## Parameters

None.

## Notes

- Deleting never refunds free questions — the quota ledger is a standalone counter, not a count of messages.
- Deleting the conversation on screen falls back to a new one ([ai_chat_new_conversation_clicked](ai_chat_new_conversation_clicked.md) is not logged for that fallback).
- Propagates to the user's other devices over Realtime.
