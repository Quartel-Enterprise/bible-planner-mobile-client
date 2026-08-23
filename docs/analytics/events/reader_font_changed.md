# reader_font_changed

**Tier:** P2 | **Domain:** Reader

A different reading typeface was chosen. With nine bundled families, this says which ones earn their space in the app.

## When it fires

The user taps a typeface tile in the appearance sheet.

## Trigger source

`feature/read/.../presentation/appearance/ReaderAppearanceViewModel.kt` — `ReaderAppearanceUiEvent.OnFontClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `font` | string | `lora` | The typeface in use |

## Notes

- `font=dyslexic` is the accessibility option (OpenDyslexic) and is worth watching on its own.
- `font=system` ships no file: it renders with the platform's own sans-serif.
