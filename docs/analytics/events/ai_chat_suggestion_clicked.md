# ai_chat_suggestion_clicked

**Tier:** P2 | **Domain:** AiChat

Measures how much of the chat's usage is driven by the suggested questions (the day study's common questions) instead of free typing.

## When it fires

The user taps a suggestion chip, either in the initial list under the welcome message or in the collapsible bar above the input.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnSuggestionClick`

## Parameters

None.

## Notes

- Always followed by [ai_chat_message_sent](ai_chat_message_sent.md): tapping a chip sends it immediately.
- A used chip is removed from the remaining suggestions, so the same one never fires twice in a conversation.
- Suggestions only exist when the day's study is already cached locally.
