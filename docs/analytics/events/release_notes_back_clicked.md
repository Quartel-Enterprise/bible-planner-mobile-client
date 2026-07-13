# release_notes_back_clicked

**Tier:** P2 | **Domain:** Settings

Captures the user tapping the back arrow on the release-notes screen. Shows engagement depth with the release-notes screen before leaving.

## When it fires

User taps the back arrow on the release-notes screen.

## Trigger source

`feature/release_notes/src/commonMain/kotlin/com/quare/bibleplanner/feature/releasenotes/presentation/viewmodel/ReleaseNotesViewModel.kt` — `ReleaseNotesUiEvent.OnBackClicked`

## Parameters

None.

## Notes

- Destination impression for the screen itself is covered by [destination_view](destination_view.md) (`release_notes`, `screen`).
- Related: [release_notes_tab_selected](release_notes_tab_selected.md).
