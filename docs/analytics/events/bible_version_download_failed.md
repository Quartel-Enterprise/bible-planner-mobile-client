# bible_version_download_failed

**Tier:** P1 | **Domain:** BibleVersions

Captures a Bible version download aborting with an error. Failure rate per version and reason surfaces backend/content problems (a broken version manifest) versus plain connectivity loss.

## When it fires

The download pipeline returns failure for the version: the error notification is shown and the version's status flips to paused so the user can retry.

## Trigger source

`feature/preferences/bible_version/.../domain/InProcessBibleVersionDownloader.kt` — `startDownload(...)`, `onFailure` branch of `downloadBible(versionId)` (calls `BibleVersionDownloadNotifier.showError` and sets `DownloadStatus.PAUSED`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `nvi` | Bible version identifier |
| `reason` | string | `network` | Free-string failure classification derived from the caught throwable; the exact value set (e.g. `network` vs `error`) must be finalized during implementation |

## Notes

- The `onFailure` branch receives an untyped `Throwable` from `DownloadBibleUseCase` — there is no failure enum in the downloader today, so `reason` is documented as a free string; classify at least network vs everything-else when instrumenting.
- A failed download is left in the paused state; do **not** also log [bible_version_download_paused](bible_version_download_paused.md) for it (that event is user-initiated only). The user's retry then arrives as [bible_version_download_started](bible_version_download_started.md) with `is_resume=true`.
- Cancellation of the download job (e.g. `cancelDownload`) is not a failure and is not logged here.
