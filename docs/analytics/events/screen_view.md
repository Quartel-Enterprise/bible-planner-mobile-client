# screen_view

**Tier:** P1 | **Domain:** Navigation

Captures every screen, dialog and bottom sheet the user sees. This is the backbone of user traceability: it reconstructs the navigation path of a session. It uses GA4's reserved `screen_view` name with the reserved `screen_name` / `screen_class` parameters, so it feeds the built-in "Screens" report and the `unifiedScreenName` / `unifiedScreenClass` dimensions directly — path and funnel explorations work without registering custom dimensions. Using the reserved name also makes the Firebase SDK maintain the previous-screen chain (`ga_previous_screen` / `ga_previous_class`) on each hit by itself, which is what path exploration walks.

## When it fires

Whenever the top entry of the visible Navigation 3 back stack changes — a screen is pushed, popped back to, replaced, or a bottom tab is switched.

## Trigger source

Centralized, not per-destination:

- Root back stack: `core/navigation/.../RootAppNavDisplay.kt`
- Per-tab back stacks: `feature/main/.../navhost/BottomNavTabState.kt`

Both call the `TrackDestination` use case (`core/provider/analytics/.../TrackDestinationUseCase.kt`), which casts the `NavKey` to the sealed `NavRoute` (`core/model/.../route/NavRoute.kt`, implemented by every route) and maps it via `NavRouteToDestinationMapper` to a stable `screen_name` and a `screen_class`, using the table in [README.md](../README.md#navkey--screen_name--screen_class-mapping). Because the mapper's `when` is exhaustive over the sealed `NavRoute`, a new route that isn't added there fails the build. `MainNavRoute` itself is not logged (it is the tab container; the active tab's key is logged instead).

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `screen_name` | string | `book_details` | Stable destination identifier from the NavKey mapping |
| `screen_class` | string | `dialog` | `screen` \| `dialog` \| `bottom_sheet` \| `responsive` — what kind of destination is being shown, from the NavKey mapping |
| *(route args)* | varies | `book_id=genesis` | Arguments carried by the NavKey, mapped per the README table (e.g. `book_id`, `chapter_number`, `plan_type`, `week_number`, `day_number`, `reason`, `version_id`, `max_free_notes`) |

## Notes

- Dialog and bottom-sheet routes (login warning, theme selection, delete confirmations, etc.) count as destinations too — they are NavKeys on the back stack and their impressions matter (e.g. `login_warning` impressions feed the auth funnel). GA4's "screen" means "what the user is looking at", not "full-screen Activity", so these belong in the same event; `screen_class` is what separates them, letting a funnel restrict itself to `screen_class = screen` or deliberately include a dialog step.
- `screen_class` carries the **kind** of destination, not the route's class name. A class name would be obfuscated by R8 in release builds, and the kind is the axis funnels actually need. The `destination_*` vocabulary is kept in the code (`Destination`, `DestinationType`, `NavRouteToDestinationMapper`, `TrackDestination`) — only the wire names are GA4's, because they are the only names GA4 populates `unifiedScreenName` from.
- Firebase's automatic screen tracking is explicitly disabled — `google_analytics_automatic_screen_reporting_enabled=false` in `androidApp/src/main/AndroidManifest.xml` and `FirebaseAutomaticScreenReportingEnabled=false` in `iosApp/iosApp/Info.plist`. In a single-activity Compose app it only ever reported `MainActivity` as `screen_class` with no `screen_name` at all. Those empty auto-views were what put 96% of `screenPageViews` under `(not set)` in the 30 days before this rename — the navigation data itself was never missing, it was just being logged under the custom name `destination_view`, which GA4 does not read `unifiedScreenName` from.
- A few responsive settings routes render as either a dialog or a bottom sheet depending on window width; they get the dedicated `responsive` value instead of being forced into `dialog` or `bottom_sheet` (see the note under the mapping table in the README).
- **Android and iOS only for the built-in reports.** Desktop posts through the Measurement Protocol to a *Web* data stream, where `screen_view` is not a recognised app event — those hits still arrive, and `screen_name` is still queryable as an event parameter, but they do not feed `unifiedScreenName`. Desktop traffic is small and this follows the Measurement Protocol gap already documented in [README.md](../README.md#auto-collected-events); revisit only if desktop volume starts mattering.
- Keep the NavKey mapping table in the README in sync with `Nav3SavedStateConfiguration.kt` when adding routes — the compiler enforces the mapper side of this via the sealed `NavRoute` hierarchy, but the README table itself is not auto-checked.
