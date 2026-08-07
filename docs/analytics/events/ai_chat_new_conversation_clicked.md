# ai_chat_new_conversation_clicked

**Tier:** P2 | **Domain:** AiChat

Taps that start a fresh conversation, from the app-bar icon or the drawer's "New conversation" button.

## When it fires

The user taps the new-conversation icon in the chat app bar, or the FAB inside the history drawer.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnNewConversationClick`

## Parameters

None.

## Notes

- Nothing is created server-side yet: the conversation row only exists once the first question is sent.
- Also fires when the user deletes the conversation they were reading, since the screen falls back to a new one.
