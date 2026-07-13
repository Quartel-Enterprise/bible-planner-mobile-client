# bible_version_delete_cancelled

**Tier:** P2 | **Domain:** BibleVersions

Captures backing out of the delete-version dialog. Together with [bible_version_deleted](bible_version_deleted.md) it measures how often the destructive action is reached by accident vs intent.

## When it fires

User taps the cancel button in the delete-version dialog.

## Trigger source

`feature/delete_version/src/commonMain/kotlin/com/quare/bibleplanner/feature/deleteversion/presentation/viewmodel/DeleteVersionViewModel.kt` — `DeleteVersionUiEvent.OnCancel`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Dialog impressions are covered by [destination_view](destination_view.md) (`destination_name=delete_version` with `version_id`).
- Pair: [bible_version_deleted](bible_version_deleted.md).
