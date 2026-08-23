# verses_copied

**Tier:** P1 | **Domain:** Verse annotations

The passage was copied to the clipboard. Copying is invisible afterwards, so this is the only measure of it — and it is a plausible substitute for sharing.

## When it fires

The user taps Copy in the selection panel.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnCopyClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `verse_count` | integer | 3 | How many verses the selection covered |

## Notes

- The clipboard text carries the passage, its reference and the bible version.
- Nothing is reported when the selected version has no text for the passage, since there is nothing to copy.
