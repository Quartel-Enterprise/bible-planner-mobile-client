# bible_version_update_clicked

**Tier:** P1 | **Domain:** BibleVersions

Captures the user tapping the update pill on a Bible version whose downloaded text is outdated. Measures adoption of manual content updates and, combined with the prompt events, which surface drives them.

## When it fires

A downloaded version has a pending content update, so its row in the Bible version manager shows an update pill; the user taps it. The version is then wiped and re-downloaded.

## Trigger source

`feature/preferences/bible_version/.../BibleVersionViewModel.kt` — `BibleVersionUiEvent.OnUpdate`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `ACF` | The Bible version being updated. |

## Notes

- The re-download itself reports through [bible_version_download_completed](bible_version_download_completed.md) / [bible_version_download_failed](bible_version_download_failed.md), like any download.
- Updating from the startup prompt fires [bible_update_prompt_update_clicked](bible_update_prompt_update_clicked.md) instead.
