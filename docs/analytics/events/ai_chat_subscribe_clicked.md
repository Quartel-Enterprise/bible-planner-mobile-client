# ai_chat_subscribe_clicked

**Tier:** P1 | **Domain:** AiChat

Paywall entries coming from the chat: the user hit the free-question limit and tapped the locked input bar.

## When it fires

The user taps "Subscribe to keep asking", the bar that replaces the input once the free quota is exhausted.

## Trigger source

`feature/chat/.../presentation/viewmodel/ChatViewModel.kt` — `ChatUiEvent.OnSubscribeClick`

## Parameters

None.

## Notes

- Navigates to the paywall; the screen view itself is covered by [destination_view](destination_view.md).
- The chat quota is metered separately from the day-study one, so this is a distinct monetization path from [day_study_card_clicked](day_study_card_clicked.md) with `card_mode=locked`.
