# reader_appearance_opened

**Tier:** P2 | **Domain:** Reader

The reading appearance sheet was opened — the entry point to text size, typeface and the focus aids.

## When it fires

The user taps the Aa button in the reader.

## Trigger source

`feature/read/.../presentation/ReadViewModel.kt` — `ReadUiEvent.OnAppearanceClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- The sheet's impression is covered by [screen_view](screen_view.md) (`screen_name=reader_appearance`); this measures click-through rate.
