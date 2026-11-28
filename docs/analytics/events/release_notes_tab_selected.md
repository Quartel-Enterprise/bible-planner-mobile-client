# release_notes_tab_selected

**Tier:** P2 | **Domain:** Settings

Captures which tab the user opens on the release-notes screen. Shows whether users care about past changes, the latest release or the roadmap (upcoming), guiding how much effort release-notes writing deserves.

## When it fires

User switches to another tab on the release-notes screen. The ViewModel tracks it only when the tab changes, so tapping the selected tab doesn't fire it; app versions before the native iOS release notes also counted those re-taps.

## Trigger source

`feature/release_notes/src/commonMain/kotlin/com/quare/bibleplanner/feature/releasenotes/presentation/viewmodel/ReleaseNotesViewModel.kt` — `ReleaseNotesUiEvent.OnTabSelected(tab: ReleaseNotesTab)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `tab` | string | `upcoming` | Selected `ReleaseNotesTab` value: `past_versions` \| `latest` \| `upcoming` |

## Notes

- The initially selected tab does not fire this event — screen entry is covered by [screen_view](screen_view.md) (`release_notes`).
- Related: [github_release_opened](github_release_opened.md), [profile_option_clicked](profile_option_clicked.md) (`option=release_notes`).
