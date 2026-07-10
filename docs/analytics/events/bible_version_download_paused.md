# bible_version_download_paused

**Tier:** P2 | **Domain:** BibleVersions

Captures the user deliberately pausing an in-progress Bible version download — typically to save data or battery. A high pause rate on specific versions hints the download is too heavy.

## When it fires

The user taps pause on a downloading version in the selector.

## Trigger source

`feature/preferences/bible_version/.../presentation/BibleVersionViewModel.kt` — `BibleVersionUiEvent.OnPause(id)` → `BibleVersionDownloaderFacade.pauseDownload(id)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `nvi` | Bible version identifier |

## Notes

- User-initiated only. Download failures also leave the version in the paused state, but those log [bible_version_download_failed](bible_version_download_failed.md) instead — never both.
- The follow-up resume logs [bible_version_download_started](bible_version_download_started.md) with `is_resume=true`.
