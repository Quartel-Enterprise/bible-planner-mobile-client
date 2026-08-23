# verse_share_opened

**Tier:** P1 | **Domain:** Verse annotations

The share sheet was opened from the reader — the top of the sharing funnel, which is the app's main organic-referral path.

## When it fires

The user taps Share in the selection panel.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnShareClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- Ends in [verse_shared](verse_shared.md), or in [verse_share_dismissed](verse_share_dismissed.md) if the user backs out.
