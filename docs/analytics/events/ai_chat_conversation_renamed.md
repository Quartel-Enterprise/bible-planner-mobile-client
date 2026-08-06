# ai_chat_conversation_renamed

**Tier:** P3 | **Domain:** AiChat

Manual renames of a conversation, measuring whether users curate their history or live with the auto-generated titles.

## When it fires

The user confirms the inline rename field in the history drawer.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnRenameConfirm`

## Parameters

None.

## Notes

- Fires on confirm even when the field is blank (in which case the old title is kept and no request is sent).
- Conversations are named automatically by the model after the first exchange, so a rename is an explicit correction.
