# ai_chat_conversation_selected

**Tier:** P2 | **Domain:** AiChat

Reopens of an existing conversation from the history drawer.

## When it fires

The user taps a conversation row in the history drawer.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnConversationClick`

## Parameters

None.

## Notes

- The conversation id is deliberately not sent: it identifies user content and adds no analysable dimension.
- Closes the drawer and loads that thread's messages from the server.
