# highlight_custom_color_deleted

**Tier:** P1 | **Domain:** Verse annotations

A custom colour left the palette. `kept_highlights` is the interesting half: it says whether people treat the palette as a shortcut list or as ownership of what they marked.

## When it fires

The user confirms either option in the delete-colour dialog.

## Trigger source

`feature/read/.../presentation/deletecolor/DeleteHighlightColorViewModel.kt` — `DeleteHighlightColorUiEvent.OnConfirmClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `color` | string | `yellow` | The colour key: a preset name, or `c:<hue>:<lightness>` for a custom mix |
| `kept_highlights` | boolean | true | Whether the highlights made with the colour survived its deletion |

## Notes

- With `kept_highlights=false` every highlight using the colour is cleared, each as its own synced tombstone.
