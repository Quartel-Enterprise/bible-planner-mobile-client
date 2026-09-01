# reader_focus_aid_changed

**Tier:** P2 | **Domain:** Reader

The reading ruler or the focused-verse dimming was turned on or off. They are mutually exclusive, so one event with a `focus_aid` value describes all three states.

## When it fires

The user flips either switch in the appearance sheet, or dismisses the ruler with the close button on the ruler band itself.

## Trigger source

`feature/read/.../presentation/appearance/ReaderAppearanceViewModel.kt` — `ReaderAppearanceUiEvent.OnFocusAidChange`

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnRulerDismissClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `focus_aid` | string | `ruler` | `ruler`, `focused_verse` or `none` |
| `source` | string | `appearance_sheet` | Where the change came from: `appearance_sheet` or `ruler` |

## Notes

- `source=ruler` is the in-place dismissal, `source=appearance_sheet` the deliberate setting change; a high share of the former means the ruler is being turned on by accident.
- The focused-verse option is only offered on wide windows, where there is a pointer to follow.
