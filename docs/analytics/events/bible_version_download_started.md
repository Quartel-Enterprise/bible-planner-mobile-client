# bible_version_download_started

**Tier:** P1 | **Domain:** BibleVersions

Captures the user starting (or resuming) a Bible version download. Demand per version plus the resume rate shows which translations users want offline and how often large downloads get interrupted.

## When it fires

The user taps download on a version in the selector, or taps resume on a paused download.

## Trigger source

`feature/preferences/bible_version/.../presentation/BibleVersionViewModel.kt` — two UiEvents, both delegating to `BibleVersionDownloaderFacade.downloadVersion(id)`:

- `BibleVersionUiEvent.OnDownload(id)` — fresh start (`is_resume=false`)
- `BibleVersionUiEvent.OnResume(id)` — resuming a paused download (`is_resume=true`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `nvi` | Bible version identifier |
| `is_resume` | boolean | `false` | `true` when resuming a paused download, `false` on a fresh start |

## Notes

- One event with `is_resume` instead of separate start/resume events, per the toggle-style convention in the README.
- Interrupted downloads auto-resume on app start (`InProcessBibleVersionDownloader.resumePendingDownloads`); that is system-initiated and not logged — this event tracks user intent only.
- Outcomes: [bible_version_download_completed](bible_version_download_completed.md), [bible_version_download_failed](bible_version_download_failed.md), or a user pause ([bible_version_download_paused](bible_version_download_paused.md)).
