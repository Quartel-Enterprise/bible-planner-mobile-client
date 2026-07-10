# github_release_opened

**Tier:** P2 | **Domain:** Settings

Captures the user jumping from the release-notes screen to GitHub — either the full releases list or a specific version's release page. Indicates a technically engaged audience and validates keeping the GitHub links visible.

## When it fires

User taps the "all releases" GitHub link or the GitHub link of a specific version on the release-notes screen.

## Trigger source

`feature/release_notes/src/commonMain/kotlin/com/quare/bibleplanner/feature/releasenotes/presentation/viewmodel/ReleaseNotesViewModel.kt` — two trigger points:

- `ReleaseNotesUiEvent.OnGithubAllReleasesClicked`
- `ReleaseNotesUiEvent.OnGithubVersionClicked(version: String)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `version` | string | `2.4.1` | App version whose release page was opened. Omitted for `OnGithubAllReleasesClicked` (all-releases list) |

## Notes

- One event, two trigger points: presence of `version` distinguishes a specific release from the all-releases link.
- Related: [release_notes_tab_selected](release_notes_tab_selected.md).
