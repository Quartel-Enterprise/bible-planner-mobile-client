# annotations_back_clicked

**Tier:** P2 | **Domain:** Verse annotations

Leaving the Annotations screen through the top-bar back button.

## When it fires

The user taps the back arrow on the Annotations screen.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnBackClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- System back gestures do not fire this event; the resulting `screen_view` of the previous screen covers them.
