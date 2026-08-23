# reader_font_menu_toggled

**Tier:** P2 | **Domain:** Reader

The typeface list was expanded or collapsed. It measures how many people look at the fonts versus how many change one.

## When it fires

The user taps the Font row in the appearance sheet.

## Trigger source

`feature/read/.../presentation/appearance/ReaderAppearanceViewModel.kt` — `ReaderAppearanceUiEvent.OnFontMenuToggle`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_expanded` | boolean | true | Whether the font list was opened or closed |

## Notes

- Expansions without a following [reader_font_changed](reader_font_changed.md) mean the current typeface won.
