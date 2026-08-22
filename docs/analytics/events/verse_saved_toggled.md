# verse_saved_toggled

**Tier:** P1 | **Domain:** Verse annotations

Bookmarking a passage. One event with an `is_saved` boolean, so saves and unsaves stay comparable.

## When it fires

The user taps Save in the selection panel. The whole selection moves to the same state: if any selected verse was unsaved, all of them are saved.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnToggleSavedClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_saved` | boolean | true | Whether the verses ended up saved or unsaved |
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- Saving is independent of highlighting — a verse can be saved without a colour and vice versa.
