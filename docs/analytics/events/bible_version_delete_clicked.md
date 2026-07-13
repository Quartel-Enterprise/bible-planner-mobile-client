# bible_version_delete_clicked

**Tier:** P2 | **Domain:** BibleVersions

Captures the user tapping the delete action on a downloaded Bible version, which opens the delete-confirmation dialog. Shows intent to remove a downloaded version, upstream of the actual [bible_version_deleted](bible_version_deleted.md) / [bible_version_delete_cancelled](bible_version_delete_cancelled.md) outcome.

## When it fires

User taps the delete icon/action on a downloaded Bible version row in the version manager.

## Trigger source

`feature/preferences/bible_version/src/commonMain/kotlin/com/quare/bibleplanner/feature/bibleversion/presentation/BibleVersionViewModel.kt` — `BibleVersionUiEvent.OnDelete(id)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `nvi` | Bible version identifier the user tapped delete on |

## Notes

- Opens the delete-confirmation dialog; its impression is covered by [destination_view](destination_view.md) (`delete_version`, `dialog`, `version_id`).
- Related: [bible_version_deleted](bible_version_deleted.md), [bible_version_delete_cancelled](bible_version_delete_cancelled.md).
