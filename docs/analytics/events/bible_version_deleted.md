# bible_version_deleted

**Tier:** P2 | **Domain:** BibleVersions

Captures the user confirming deletion of a downloaded Bible version. Deletions shortly after download suggest storage pressure or a version that disappointed; frequent delete/re-download cycles argue for smaller or partial downloads.

## When it fires

The user confirms in the delete-version dialog and the downloaded content is removed.

## Trigger source

`feature/delete_version/.../presentation/viewmodel/DeleteVersionViewModel.kt` — `DeleteVersionUiEvent.OnConfirmDelete` → `BibleVersionDownloaderFacade.deleteDownload(versionId)`

The dialog itself is reached from `feature/preferences/bible_version/.../presentation/BibleVersionViewModel.kt` — `BibleVersionUiEvent.OnDelete(id)`, which only navigates to `DeleteVersionNavRoute` (not logged; the confirmation is the event).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `nvi` | Bible version identifier (from `DeleteVersionNavRoute.versionId`) |

## Notes

- Dialog impressions (and implicitly the cancel rate) are covered by [destination_view](destination_view.md) (`destination_name=delete_version` with `version_id`); `DeleteVersionUiEvent.OnCancel` is deliberately not tracked.
- A later re-download shows up as [bible_version_download_started](bible_version_download_started.md) with `is_resume=false`.
