# release_notes_tab_selected

**Tier:** P2 | **Domain:** Settings

Captures which tab the user opens on the release-notes screen. Shows whether users care about past changes, the latest release or the roadmap (upcoming), guiding how much effort release-notes writing deserves.

## When it fires

User taps one of the three tabs on the release-notes screen.

## Trigger source

`feature/release_notes/src/commonMain/kotlin/com/quare/bibleplanner/feature/releasenotes/presentation/viewmodel/ReleaseNotesViewModel.kt` — `ReleaseNotesUiEvent.OnTabSelected(tab: ReleaseNotesTab)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `tab` | string | `upcoming` | Selected `ReleaseNotesTab` value: `past_versions` \| `latest` \| `upcoming` |

## Notes

- The initially selected tab does not fire this event — screen entry is covered by [destination_view](destination_view.md) (`release_notes`).
- Related: [github_release_opened](github_release_opened.md), [profile_option_clicked](profile_option_clicked.md) (`option=release_notes`).
