# read_retry_clicked

**Tier:** P2 | **Domain:** Reading

Captures the user tapping "retry" after chapter content fails to load on the Read screen. Retry volume signals how often content loading fails from the user's perspective.

## When it fires

The Read screen fails to load chapter content and shows a retry affordance; the user taps it.

## Trigger source

`feature/read/.../ReadViewModel.kt` — `ReadUiEvent.OnRetryClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Only the click is tracked, not the retry's outcome.
