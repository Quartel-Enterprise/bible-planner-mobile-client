# verse_share_streak_toggled

**Tier:** P2 | **Domain:** Verse annotations

The reading streak badge was added to or removed from the card. Says whether people want to show their own progress alongside the verse.

## When it fires

The user taps Add streak or Remove streak in the image composer. The chip only appears when the selected plan has a streak of at least one day.

## Trigger source

`feature/verse/share/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnStreakToggleClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | string | `"true"` | Whether the badge is on the card after the tap |
| `streak_days` | integer | 12 | The streak shown on the badge |
