# bible_version_update_prompt_update_clicked

**Tier:** P1 | **Domain:** BibleVersions

Captures the user accepting the startup prompt that lists Bible versions with pending content updates. Together with [bible_version_update_prompt_dismissed](bible_version_update_prompt_dismissed.md) it gives the prompt's acceptance rate.

## When it fires

The app opens, at least one downloaded version has outdated content and the 4-hour dismissal cooldown has elapsed, so the pending-updates sheet is shown; the user taps its update action. All listed versions are wiped and re-downloaded.

## Trigger source

`feature/preferences/bible_version/.../PendingBibleUpdatesViewModel.kt` — `PendingBibleUpdatesUiEvent.OnUpdateClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Updating a single version from the manager list fires [bible_version_update_clicked](bible_version_update_clicked.md) instead.
