# ai_chat_retry_clicked

**Tier:** P2 | **Domain:** AiChat

Taps on "Try again" in the error card, measuring how often users recover from a failed answer.

## When it fires

The user taps the retry button of the chat's error card.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnRetryClick`

## Parameters

None.

## Notes

- Re-sends the same question; expect a following [ai_chat_message_sent](ai_chat_message_sent.md)-shaped outcome ([ai_chat_answer_failed](ai_chat_answer_failed.md) when it fails again).
- The rate-limit card only shows this button once the countdown reaches zero.
