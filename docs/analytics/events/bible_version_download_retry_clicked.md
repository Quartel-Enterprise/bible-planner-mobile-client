# bible_version_download_retry_clicked

**Tier:** P2 | **Domain:** BibleVersions

Captures the user tapping "try again" after the Bible version list failed to load. Retry volume signals how often the initial fetch fails from the user's perspective.

## When it fires

The bible version manager fails to load its list and shows a retry affordance; the user taps it.

## Trigger source

`feature/preferences/bible_version/.../BibleVersionViewModel.kt` — `BibleVersionUiEvent.TryToDownloadBibleVersionsAgain`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

None.

## Notes

- Distinct from [bible_version_download_started](bible_version_download_started.md), which is a per-version download click, not this list-level retry.
