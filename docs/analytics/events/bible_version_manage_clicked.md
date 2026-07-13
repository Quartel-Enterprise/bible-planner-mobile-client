# bible_version_manage_clicked

**Tier:** P2 | **Domain:** BibleVersions

Captures the user opening Bible version management from the Read screen. Shows how often the version selector is reached from within an active reading session, as opposed to from Settings.

## When it fires

The user taps the "manage Bible versions" action on the Read screen, opening the Bible version selector.

## Trigger source

`feature/read/.../presentation/model/ReadUiEvent.kt` — `ReadUiEvent.ManageBibleVersions`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `source` | string | `read_screen` | Surface the manage action was triggered from |

## Notes

- The resulting selector screen's own impression is covered by `destination_view` (`destination_name=bible_version_selector`); this event captures the entry point instead.
