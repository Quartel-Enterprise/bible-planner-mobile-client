# verse_shared

**Tier:** P1 | **Domain:** Verse annotations

A passage was handed to the system share sheet. `format` says whether people prefer plain text or the image card, which is what justifies maintaining the card composer.

## When it fires

The user taps As text, or Share in the image composer.

## Trigger source

`feature/share_verse/.../presentation/ShareVerseViewModel.kt` — `ShareVerseUiEvent.OnShareAsTextClick` and `ShareVerseUiEvent.OnShareImageReady`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `format` | string | `text` | `text` or `image` |
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- Logged when the system share sheet is requested; whether the user completes the share there is not observable.
- With `format=image`, the PNG is rendered from the same composable the user was previewing.
