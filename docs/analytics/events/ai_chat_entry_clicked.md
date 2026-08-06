# ai_chat_entry_clicked

**Tier:** P1 | **Domain:** AiChat

Captures every tap on an entry point of the AI chat, split by which surface it came from. This is the head of the chat funnel and, for signed-out users, a login driver.

## When it fires

The user taps the "Ask the AI" FAB on the day screen, or the "Ask about this reading" card at the bottom of the study's questions tab.

## Trigger source

`feature/day/.../presentation/viewmodel/DayViewModel.kt` — `DayUiEvent.OnAskAiClick`
`feature/day_study/.../presentation/viewmodel/DayStudyRouteViewModel.kt` — `DayStudyRouteUiEvent.OnAskAiClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `day_fab` | `day_fab` \| `day_study_questions` (snake_case of `ChatEntrySource`) |
| `is_logged_in` | boolean | `true` | Whether a user is signed in — the chat requires an account |

## Notes

- Signed-out taps route to the login warning (`reason=ai_chat`) instead of the chat and never create a conversation.
- Exhausted quota does **not** block the entry: the chat opens with its input locked, so the paywall is reached with the conversation in view.
- Funnel: `ai_chat_entry_clicked` → [destination_view](destination_view.md) (`ai_chat`) → [ai_chat_message_sent](ai_chat_message_sent.md).
