# notes_limit_reached

**Tier:** P1 | **Domain:** Notes

Captures a free user hitting the notes limit — the moment the free-tier gate becomes visible. This is the top of the notes-driven upgrade funnel and directly measures how often the limit converts curiosity into paywall traffic.

## When it fires

A free user focuses the notes field on a day without an existing note while already at the maximum number of free notes; the app clears focus and opens the AddNotesFreeWarning dialog.

## Trigger source

`feature/day/src/commonMain/kotlin/com/quare/bibleplanner/feature/day/presentation/viewmodel/DayViewModel.kt` — `DayUiEvent.OnNotesFocus` (blocked path: `useCases.shouldBlockAddNotes()` returns `true` and `AddNotesFreeWarningNavRoute` is opened)

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `max_free_notes` | int | `3` | Free-tier limit in effect (`GetMaxFreeNotesAmountUseCase`) |

## Notes

- `ShouldBlockAddNotesUseCase` gates on `isFreeUser() && daysWithNotes >= maxFreeNotes`, so this event never fires for Pro users.
- Focusing a day that already has a note never blocks (editing existing notes is always allowed) and must not fire this event.
- The dialog's own impressions are also covered by [destination_view](destination_view.md) (`add_notes_free_warning`); this event exists so the funnel step has a stable name independent of navigation. Follow-up steps: `paywall_viewed` with `source=notes_limit` if the user taps subscribe (`AddNotesFreeWarningUiEvent.OnSubscribeToPro`).
- Related: [note_saved](note_saved.md), [note_deleted](note_deleted.md) (deleting frees a slot).
