# ai_chat_entry_clicked

**Tier:** P1 | **Domain:** AiChat

Captures every tap on an entry point of the AI chat, split by which surface it came from. This is the head of the chat funnel.

## When it fires

The user taps the "Ask the AI" FAB on the day screen, or the "Ask about this reading" card at the bottom of the study's questions tab.

## Trigger source

`feature/day/.../presentation/viewmodel/DayViewModel.kt` — `DayUiEvent.OnAskAiClick`
`feature/day_study/.../presentation/viewmodel/DayStudyRouteViewModel.kt` — `DayStudyRouteUiEvent.OnAskAiClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `day_fab` | `day_fab` \| `day_study_questions` (snake_case of `ChatEntrySource`) |

## Notes

- Nothing gates the entry — being signed out or out of free questions still opens the chat. Both are asked for at the moment the reader tries to send: signing in via the login warning (`reason=ai_chat`, covered by [screen_view](screen_view.md)), subscribing via the locked input ([ai_chat_subscribe_clicked](ai_chat_subscribe_clicked.md)). So the gap between this event and [ai_chat_message_sent](ai_chat_message_sent.md) is where both gates show up.
- Funnel: `ai_chat_entry_clicked` → [screen_view](screen_view.md) (`ai_chat`) → [ai_chat_message_sent](ai_chat_message_sent.md).
