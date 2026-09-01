# bible_update_prompt_version_toggled

**Tier:** P2 | **Domain:** BibleVersions

Captures the user checking or unchecking a version in the startup prompt that lists Bible versions with pending content updates. Shows whether users curate the selection or accept the default of everything checked.

## When it fires

The pending-updates sheet is open and the user taps a version row or its checkbox, toggling whether that version will be included in the update.

## Trigger source

`feature/preferences/bible_version/.../PendingBibleUpdatesViewModel.kt` — `PendingBibleUpdatesUiEvent.OnToggleVersion`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `ACF` | The Bible version being toggled. |
| `is_selected` | boolean | `false` | Whether the version is selected after the toggle. |

## Notes

- All listed versions start selected; the update action ([bible_update_prompt_update_clicked](bible_update_prompt_update_clicked.md)) only applies to the ones still selected.
