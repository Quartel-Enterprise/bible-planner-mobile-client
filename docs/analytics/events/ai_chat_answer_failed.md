# ai_chat_answer_failed

**Tier:** P1 | **Domain:** AiChat

Failure counterpart of [ai_chat_message_sent](ai_chat_message_sent.md): why an answer did not arrive, split by cause.

## When it fires

The answer stream ends in an error: no connection or a server failure, the free quota being exhausted, or the per-user rate limit.

## Trigger source

`feature/chat/.../domain/coordinator/ChatStreamCoordinatorImpl.kt`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `reason` | string | `generic` | `generic` \| `limit_reached` \| `rate_limited` |

## Notes

- `limit_reached` (HTTP 402) locks the input and turns it into the paywall entry; `rate_limited` (HTTP 429) starts a countdown and re-enables the input at zero.
- The question is dropped from the conversation on failure (the server deletes it too), so a retry re-asks rather than duplicating it.
- Fires from the coordinator, which runs outside the screen: it is logged even if the user navigated away mid-answer.
