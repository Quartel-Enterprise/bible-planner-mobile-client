# ai_chat_suggestion_clicked

**Tier:** P2 | **Domain:** AiChat

Measures how much of the chat's usage is driven by the suggested questions instead of free typing.

## When it fires

The user taps a suggestion chip, either in the initial list under the welcome message or in the collapsible bar above the input.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnSuggestionClick`

## Parameters

None.

## Notes

- Always followed by [ai_chat_message_sent](ai_chat_message_sent.md): tapping a chip sends it immediately.
- A used chip is removed from the remaining suggestions, so the same one never fires twice in a conversation.
- Which chips are offered depends on where the chat was opened from, not on what happens to be cached: from the study's questions tab they are that study's own common questions; from the day screen they are a fixed set of starters, so the same button always behaves the same way. The starters also stand in if a study somehow has no questions.
