# reader_vertical_reading_toggled

**Tier:** P2 | **Domain:** Reader

Continuous reading — appending the next chapter to the same scroll — was turned on or off.

## When it fires

The user flips the Vertical reading switch in the appearance sheet.

## Trigger source

`feature/read/.../presentation/appearance/ReaderAppearanceViewModel.kt` — `ReaderAppearanceUiEvent.OnVerticalReadingChange`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | boolean | true | The state the setting was moved to |

## Notes

- While on, the reader loads two chapters at once, so [chapter_read_toggled](chapter_read_toggled.md) can arrive for either of them.
