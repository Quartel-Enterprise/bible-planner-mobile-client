# bible_version_selected

**Tier:** P1 | **Domain:** BibleVersions

Captures the user switching the active Bible version. Version popularity guides which translations to prioritize, license and pre-bundle.

## When it fires

The user taps a downloaded version in the Bible version selector, making it the active reading version.

## Trigger source

`feature/preferences/bible_version/.../presentation/BibleVersionViewModel.kt` — `BibleVersionUiEvent.OnSelect(id)` → `SetSelectedVersionUseCase`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version_id` | string | `nvi` | Bible version identifier |

## Notes

- Only downloaded versions are selectable; selecting is independent of downloading ([bible_version_download_started](bible_version_download_started.md)).
- Selector impressions are covered by [destination_view](destination_view.md) (`destination_name=bible_version_selector`).
- Re-selecting the already-active version still emits the UiEvent; decide during implementation whether to dedupe (recommended: log only actual changes).
