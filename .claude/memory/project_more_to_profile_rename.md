---
name: project_more_to_profile_rename
description: "The \"More\" tab became \"Profile\" (module, packages, route, GA4 events, Remote Config keys) on 2026-07-21, with old Firebase RC keys kept as deprecated."
metadata: 
  node_type: memory
  type: project
  originSessionId: c860573a-5169-4962-9498-da321e58194b
  modified: 2026-07-21T04:30:42.175Z
---

On 2026-07-21 the whole "More" surface was renamed to "Profile": module `feature/more` → `feature/profile` (package `feature.profile`, `featureProfileModule` in Koin to avoid clashing with core's `profileModule`), `MainNavRouteDestination.More` → `.Profile`, and the bottom-nav item now renders the user's avatar in 3 states (photo → initials → `Icons.Default.Person`) via `ProfileAvatar(fallbackIcon = ...)`.

The user chose to rename the analytics names too, so GA4 series break at that date: `more_option_clicked` → `profile_option_clicked`, screen `more` → `profile`, tab `more` → `profile`, param source `more_menu` → `profile_menu`.

Firebase Remote Config (project `bible-planner-98ad6`, template v29) now has `profile_web_app_enabled` / `profile_web_app_url`; the old `more_web_app_enabled` / `more_web_app_url` were **kept and marked Deprecated in their description** so already-published app versions keep working. Delete them only once those versions are out of circulation.

**Why:** future readers will see duplicated RC keys and a discontinuity in GA4 dashboards and need to know both are intentional.

**How to apply:** when touching web-app remote config or comparing GA4 history around Jul/2026, account for both key/event names. See [[project_remoteconfig_observe_first]].
