# reader_note_icon_toggled

**Tier:** P2 | **Domain:** Reader

The icon that marks annotated verses in the reader was turned on or off.

## When it fires

The user flips the Note icon switch in the appearance sheet.

## Trigger source

`feature/read/.../presentation/appearance/ReaderAppearanceViewModel.kt` — `ReaderAppearanceUiEvent.OnNoteIconChange`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_enabled` | boolean | false | The state the setting was moved to |

## Notes

- The setting starts on, so `is_enabled=false` is the signal that the icons get in the way of reading.
- While off, [verse_note_icon_clicked](verse_note_icon_clicked.md) cannot fire.
