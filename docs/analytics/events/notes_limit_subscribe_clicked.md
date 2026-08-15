# notes_limit_subscribe_clicked

**Tier:** P1 | **Domain:** Notes

Captures the user accepting the upsell on the free-notes limit dialog. It is the click half of the notes monetization gate: [notes_limit_reached](notes_limit_reached.md) measures how often users hit the wall, this measures how many of them act on it.

## When it fires

The user taps the subscribe button on the free-notes limit dialog.

## Trigger source

`feature/add_notes_free_warning/.../presentation/model/AddNotesFreeWarningUiEvent.kt` — `AddNotesFreeWarningUiEvent.OnSubscribeToPro`

## Parameters

None.

## Notes

- Pairs with [add_notes_free_warning_dismissed](add_notes_free_warning_dismissed.md), the cancel half of the same dialog.
- Navigates to the paywall replacing the dialog on the stack, so the resulting impression is [paywall_viewed](paywall_viewed.md) with `source=notes_limit`.
- Funnel: [notes_limit_reached](notes_limit_reached.md) → `notes_limit_subscribe_clicked` → [paywall_viewed](paywall_viewed.md) → [purchase_completed](purchase_completed.md).
