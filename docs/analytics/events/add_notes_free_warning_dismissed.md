# add_notes_free_warning_dismissed

**Tier:** P2 | **Domain:** Notes

Captures a free user declining to upgrade after hitting the notes limit. Together with [notes_limit_reached](notes_limit_reached.md) and `paywall_viewed` (`source=notes_limit`) it measures the drop-off at the top of the notes-driven upgrade funnel.

## When it fires

User taps the cancel button in the AddNotesFreeWarning dialog instead of subscribing.

## Trigger source

`feature/add_notes_free_warning/src/commonMain/kotlin/com/quare/bibleplanner/feature/addnotesfreewarning/presentation/viewmodel/AddNotesFreeWarningViewModel.kt` — `AddNotesFreeWarningUiEvent.OnCancel`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|

No parameters.

## Notes

- Dialog impressions are covered by [destination_view](destination_view.md) (`destination_name=add_notes_free_warning` with `max_free_notes`).
- The alternative path (`AddNotesFreeWarningUiEvent.OnSubscribeToPro`) logs [paywall_viewed](paywall_viewed.md) with `source=notes_limit` instead of this event.
- Related: [notes_limit_reached](notes_limit_reached.md).
