# reader_ruler_height_changed

**Tier:** P2 | **Domain:** Reader

Captures the reader resizing the reading ruler's band. The band covers one line by default; widening it says the ruler is being used to hold a whole verse or paragraph rather than a single line, which is what tells us whether the default is the right one.

## When it fires

The user drags the "Band height" slider in the reading appearance sheet. The row only exists while the ruler is on, so every occurrence comes from someone actively reading with it.

## Trigger source

`feature/read/.../presentation/appearance/ReaderAppearanceViewModel.kt` — `ReaderAppearanceUiEvent.OnRulerLinesChange`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `line_count` | number | `2` | Lines of text the band now covers, 1 to 4. |

## Notes

- Fires once per committed value, not continuously while the slider moves.
- Turning the ruler itself on or off is [reader_focus_aid_changed](reader_focus_aid_changed.md), which also covers the focused-verse aid the ruler competes with.
- Where the band sits is not tracked: it is dragged constantly while reading, and the position means nothing without the scroll offset it was dropped at.
