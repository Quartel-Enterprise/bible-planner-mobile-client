# highlight_custom_color_delete_opened

**Tier:** P2 | **Domain:** Verse annotations

The user long-pressed a custom colour, reaching the dialog that offers to keep or discard the highlights made with it.

## When it fires

A long press on a custom swatch in the palette row.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnCustomColorLongClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Ends in [highlight_custom_color_deleted](highlight_custom_color_deleted.md) or [highlight_custom_color_delete_cancelled](highlight_custom_color_delete_cancelled.md).
