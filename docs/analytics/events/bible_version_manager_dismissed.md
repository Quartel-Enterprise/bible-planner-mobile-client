# bible_version_manager_dismissed

**Tier:** P2 | **Domain:** BibleVersions

Captures the user closing the Bible version manager sheet. Shows how often the sheet is opened and closed without further action.

## When it fires

User dismisses the Bible version manager sheet (scrim tap, close action, or system back).

## Trigger source

`feature/preferences/bible_version/src/commonMain/kotlin/com/quare/bibleplanner/feature/bibleversion/presentation/BibleVersionViewModel.kt` — `BibleVersionUiEvent.OnDismiss`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [destination_view](destination_view.md) (`bible_version_selector`, `responsive`).
- Related: [bible_version_selected](bible_version_selected.md), [bible_version_manage_clicked](bible_version_manage_clicked.md).
