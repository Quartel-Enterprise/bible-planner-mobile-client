# bible_version_update_prompt_dismissed

**Tier:** P2 | **Domain:** BibleVersions

Captures the user declining the startup prompt that lists Bible versions with pending content updates. High dismissal volume relative to [bible_version_update_prompt_update_clicked](bible_version_update_prompt_update_clicked.md) signals the prompt is annoying or badly timed.

## When it fires

The pending-updates sheet is shown at app open and the user taps its dismiss action or closes the sheet. The prompt is then suppressed for 4 hours.

## Trigger source

`feature/preferences/bible_version/.../PendingBibleUpdatesViewModel.kt` — `PendingBibleUpdatesUiEvent.OnDismissClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Closing the sheet via its close affordance counts as a dismissal and starts the same cooldown.
