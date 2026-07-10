# screen_view

**Tier:** P1 | **Domain:** Navigation

Captures every screen, dialog and bottom sheet the user sees. This is the backbone of user traceability: it reconstructs the navigation path of a session and feeds screen-level engagement reports in GA4. It uses the standard GA4 `screen_view` event name so Firebase screen reports work out of the box.

## When it fires

Whenever the top entry of the visible Navigation 3 back stack changes — a screen is pushed, popped back to, replaced, or a bottom tab is switched.

## Trigger source

Centralized, not per-screen:

- Root back stack: `core/navigation/.../RootAppNavDisplay.kt`
- Per-tab back stacks: `feature/main/.../navhost/BottomNavTabState.kt`

The NavKey of the top entry is mapped to a stable `screen_name` using the table in [README.md](../README.md#navkey--screen_name-mapping). `MainNavRoute` itself is not logged (it is the tab container; the active tab's key is logged instead).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `screen_name` | string | `book_details` | Stable screen identifier from the NavKey mapping |
| *(route args)* | varies | `book_id=genesis` | Arguments carried by the NavKey, mapped per the README table (e.g. `book_id`, `chapter_number`, `plan_type`, `week_number`, `day_number`, `reason`, `version_id`, `max_free_notes`) |

## Notes

- Dialog and bottom-sheet routes (login warning, theme selection, delete confirmations, etc.) count as screens — they are NavKeys on the back stack and their impressions matter (e.g. `login_warning` impressions feed the auth funnel).
- Firebase's automatic screen tracking does not work here (single-activity Compose app), so automatic collection should stay disabled/ignored in favor of this event.
- Keep the NavKey mapping table in the README in sync with `Nav3SavedStateConfiguration.kt` when adding routes.
