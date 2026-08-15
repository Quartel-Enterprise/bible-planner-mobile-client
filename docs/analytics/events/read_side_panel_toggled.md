# read_side_panel_toggled

**Tier:** P2 | **Domain:** Reader

The reader's side panel was opened or closed on a wide window. It says whether the panel earns the horizontal space it takes.

## When it fires

The user taps the panel button in the wide reader header.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnSidePanelToggleClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `is_open` | boolean | false | The state the panel was moved to |

## Notes

- Selecting a verse re-opens the panel on its own; that is not reported here, since it was not a tap on the toggle.
- Narrow windows have no panel: the selection tools sit above the bottom bar instead.
