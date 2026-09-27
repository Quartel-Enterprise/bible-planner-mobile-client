# annotation_share_opened

**Tier:** P1 | **Domain:** Verse annotations

Opening the share sheet from the Annotations screen.

## When it fires

The user picks Share in a row's menu.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnShareClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 1 | How many verses the passage covers |

## Notes

- The share sheet itself reports [verse_shared](verse_shared.md) or [verse_share_dismissed](verse_share_dismissed.md).
