# reader_font_size_changed

**Tier:** P2 | **Domain:** Reader

The reader's text size was changed. The distribution of `font_size` is what tells us whether the default is set right.

## When it fires

The user releases the text size slider. Dragging is not reported — only the value the user settled on.

## Trigger source

`feature/read/.../presentation/appearance/ReaderAppearanceViewModel.kt` — `ReaderAppearanceUiEvent.OnFontSizeChangeFinished`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `font_size` | float | 19.5 | The chosen text size, in sp |

## Notes

- The value is in sp and clamped to the slider's range (14–24) before being stored.
