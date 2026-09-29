# annotations_other_version_used

**Tier:** P1 | **Domain:** Verse annotations

Switching the selected Bible version from the annotations screen to see the marks made in another version. Annotations belong to the version they were made in, and the selected version is local to each device, so this shows how often users land on a version that hides their marks.

## When it fires

The user taps Use <version> on the notice that lists the marks kept in other Bible versions. The notice replaces the empty state when the selected version has no marks, and sits above the list otherwise.

## Trigger source

`feature/verse/annotations/.../presentation/viewmodel/AnnotationsViewModel.kt` — `AnnotationsUiEvent.OnUseVersionClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | A21 | Id of the Bible version the user switched to |
| `source` | string | empty_state | `empty_state` when the selected version had no marks, `list` when the notice sat above the list |

## Notes

- Changes the selected version for the whole app, not just this screen: the reader follows it too.
