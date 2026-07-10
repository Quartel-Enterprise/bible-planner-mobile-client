# destination_view

**Tier:** P1 | **Domain:** Navigation

Captures every screen, dialog and bottom sheet the user sees. This is the backbone of user traceability: it reconstructs the navigation path of a session. Unlike GA4's reserved `screen_view` event, `destination_view` is a custom event name, so it does **not** feed GA4's automatic "Screens" report — funnels and engagement-per-destination need to be built as custom GA4 explorations against this event instead.

## When it fires

Whenever the top entry of the visible Navigation 3 back stack changes — a screen is pushed, popped back to, replaced, or a bottom tab is switched.

## Trigger source

Centralized, not per-destination:

- Root back stack: `core/navigation/.../RootAppNavDisplay.kt`
- Per-tab back stacks: `feature/main/.../navhost/BottomNavTabState.kt`

Both call the `TrackDestination` use case (`core/provider/analytics/.../TrackDestinationUseCase.kt`), which casts the `NavKey` to the sealed `NavRoute` (`core/model/.../route/NavRoute.kt`, implemented by every route) and maps it via `NavRouteToDestinationMapper` to a stable `destination_name` and a `destination_type`, using the table in [README.md](../README.md#navkey--destination_name--destination_type-mapping). Because the mapper's `when` is exhaustive over the sealed `NavRoute`, a new route that isn't added there fails the build. `MainNavRoute` itself is not logged (it is the tab container; the active tab's key is logged instead).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `destination_name` | string | `book_details` | Stable destination identifier from the NavKey mapping |
| `destination_type` | string | `dialog` | `screen` \| `dialog` \| `bottom_sheet` \| `responsive` — what kind of destination is being shown, from the NavKey mapping |
| *(route args)* | varies | `book_id=genesis` | Arguments carried by the NavKey, mapped per the README table (e.g. `book_id`, `chapter_number`, `plan_type`, `week_number`, `day_number`, `reason`, `version_id`, `max_free_notes`) |

## Notes

- Dialog and bottom-sheet routes (login warning, theme selection, delete confirmations, etc.) count as destinations too — they are NavKeys on the back stack and their impressions matter (e.g. `login_warning` impressions feed the auth funnel). `destination_type` lets funnels filter these out from full screens when needed.
- Firebase's automatic screen tracking does not work here (single-activity Compose app), so automatic collection should stay disabled/ignored in favor of this event. Because the event is named `destination_view` rather than the reserved `screen_view`, GA4 will not populate its built-in "Screens" report from it — this is a deliberate tradeoff in exchange for naming consistent with the rest of this codebase's `destination_*` vocabulary.
- A few responsive settings routes render as either a dialog or a bottom sheet depending on window width; they get the dedicated `responsive` value instead of being forced into `dialog` or `bottom_sheet` (see the note under the mapping table in the README).
- Keep the NavKey mapping table in the README in sync with `Nav3SavedStateConfiguration.kt` when adding routes — the compiler enforces the mapper side of this via the sealed `NavRoute` hierarchy, but the README table itself is not auto-checked.
