# annotation_removal_opened

**Tier:** P2 | **Domain:** Verse annotations

The removal confirmation of an annotation was opened.

## When it fires

The user picks Remove in a row's menu.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnRemoveClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Ends in [annotation_removal_confirmed](annotation_removal_confirmed.md) or [annotation_removal_cancelled](annotation_removal_cancelled.md).
