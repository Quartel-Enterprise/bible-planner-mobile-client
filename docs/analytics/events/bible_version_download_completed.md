# bible_version_download_completed

**Tier:** P1 | **Domain:** BibleVersions

Captures a Bible version download finishing successfully — the version is now fully available offline. Completion rate against [bible_version_download_started](bible_version_download_started.md) measures how well the download pipeline survives real-world networks.

## When it fires

All chapters of the version have been downloaded and the completion notification is shown.

## Trigger source

`feature/preferences/bible_version/.../domain/InProcessBibleVersionDownloader.kt` — `startDownload(...)`, `onSuccess` branch of `downloadBible(versionId)`, alongside the `BibleVersionDownloadNotifier.showComplete` call (`core/books/.../domain/BibleVersionDownloadNotifier.kt`)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `nvi` | Bible version identifier |

## Notes

- Fires in the app process even if the user navigated away from the selector (downloads run on an app-scoped coroutine scope); it will not fire if the app is killed mid-download — the download resumes on next launch and completes then.
- A resumed download produces one completion for possibly several [bible_version_download_started](bible_version_download_started.md) events; join on `version_id` and take the last start when measuring duration.
- The platform notification tap that may follow is a Settings-domain concern (`notification_opened`), not part of this event.
