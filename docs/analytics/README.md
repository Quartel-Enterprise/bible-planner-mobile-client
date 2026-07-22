# Analytics

This folder is the source of truth for every analytics event the app tracks (or plans to track). The goal is maximum traceability of user behavior: every screen the user sees and every meaningful action they take should produce an event, so funnels (reading progress, monetization, auth) and feature usage can be analyzed in Firebase Analytics / GA4.

Each event has its own file under [events/](events/) describing what it captures, when it fires, where in the code it triggers, and its parameters. This README defines the shared conventions those files rely on.

## Architecture

Events flow through the `AnalyticsService` abstraction in `core/provider/analytics`:

- **Android**: `AndroidAnalyticsService` delegates to the Firebase Analytics SDK.
- **iOS**: `IosAnalyticsService.swift` delegates to the Firebase iOS SDK (linked via SPM), injected through Koin.
- **Desktop (JVM)**: `DesktopAnalyticsService` posts directly to GA4 via the Measurement Protocol (`MeasurementProtocolClient`), since Firebase has no JVM SDK.

`AnalyticsService` exposes `setUserProperty` and `logEvent(name, params)`. ViewModels never touch `AnalyticsService` directly — they go through the `TrackEvent` use case (`core/provider/analytics/.../domain/usecase/TrackEvent.kt`). This folder is the source of truth for *what* to track; the sections below describe *how* tracking is enforced in code.

### Compile-time enforcement: every `UiEvent` declares its analytics decision

A user action can only ship without analytics if someone consciously decides so — the compiler enforces it. Every `XxxUiEvent` (the user-intent type each ViewModel receives via `onEvent`) implements the `UiEvent` marker (`ui/utils/.../presentation/UiEvent.kt`), which has one abstract member:

```kotlin
interface UiEvent {
    val analytics: EventAnalytics
}
```

Because the member is abstract, **adding a new `UiEvent` case that doesn't declare `analytics` is a compile error** — the same guarantee `NavRouteToDestinationMapper` gives for `destination_view`. The top-level split (`EventAnalytics`, in `core/provider/analytics/.../domain/model/`) answers whether the event is tracked at all:

- `Track` — the event is tracked. Splits on *how*:
  - `Track.Automatic(name, params)` — the event and its params are fully known from the event itself. The base ViewModel emits it automatically; do **not** also call `trackEvent` manually.
  - `Track.Manual(names)` — the event *is* tracked, but inside the `when` branch, because its params only exist after a domain call (e.g. `is_read` is the result of a toggle) or depend on `UiState`. Keep the `trackEvent(...)` call(s) in the branch; `names` lists every event name the branch may emit for that case.
- `NotTracked` — genuinely not a trackable action: UI plumbing only (scroll, menu open/close, animation callbacks, retry; see "Explicitly not tracked" below). No business action should ever be classified this way — if a case neither navigates nor calls `trackEvent`, that's a gap, not a `NotTracked`.

**Every click gets its own event, even when it also triggers navigation.** A click and the
resulting `destination_view` are different funnel signals — click-through rate vs. screen-view
rate are not the same number, and collapsing them loses the distinction. There is no "covered by
navigation" opt-out: a click that opens a screen/dialog/sheet gets its own `Track` event in
addition to whatever `destination_view` also logs. This applies uniformly, including bottom-tab/
nav-rail clicks (`bottom_tab_clicked`).

ViewModels that receive user intents extend `TrackedViewModel<XxxUiEvent>` (`ui/utils/.../presentation/TrackedViewModel.kt`), passing `TrackEvent` to the base and implementing `handleEvent` instead of `onEvent`. The base reads `event.analytics`, auto-emits any `Track.Automatic`, then delegates to `handleEvent`.

### Automated enforcement: `Track.Manual` names must actually be wired

Declaring a decision doesn't prove it's true, so `AnalyticsCatalogTest` (`core/provider/analytics/src/jvmTest/.../AnalyticsCatalogTest.kt`) statically checks the whole repo on every run of that module's `jvmTest`:

- every name listed in a `Track.Manual(names)` must appear in an actual `trackEvent(...)` call somewhere else in the same Gradle module (proof the wiring exists, not just the declaration);
- every name declared via `Track.Automatic` or `Track.Manual` anywhere must have a matching `docs/analytics/events/<name>.md` catalog entry.

Because this test reads files outside its own module's normal Gradle inputs, `core/provider/analytics/build.gradle.kts` explicitly declares `feature/` and `docs/analytics/events/` as task inputs for `jvmTest` — without that, Gradle's up-to-date check wouldn't notice changes to other modules and the guarantee would silently stop firing.

## Naming conventions

- Event and parameter names are `snake_case`, `<object>_<action>` with the action in past tense (`chapter_read_toggled`, `purchase_completed`).
- GA4 limits: event names ≤ 40 chars, ≤ 25 parameters per event, parameter values ≤ 100 chars.
- Toggles are **one** event with an `is_*` boolean parameter (`is_read`, `is_favorite`), never two separate on/off events.
- Confirmation dialogs use the `*_confirmed` / `*_cancelled` suffix pair.
- Enum-valued parameters use `snake_case` values (`day_screen`, `user_cancelled`).

## Standard parameter dictionary

Parameters shared across many events are defined once here; event files reference them by name.

| Parameter | Type | Values / example | Description |
|---|---|---|---|
| `destination_name` | string | `read` | Stable destination identifier (see mapping below) |
| `destination_type` | string | `screen` \| `dialog` \| `bottom_sheet` \| `responsive` | What kind of destination is being shown (see mapping below) |
| `plan_type` | string | `chronological` \| `books` | Active reading plan |
| `week_number` | int | `12` | 1-based week within the plan |
| `day_number` | int | `3` | 1-based day within the week |
| `book_id` | string | `genesis` | Bible book identifier |
| `chapter_number` | int | `5` | 1-based chapter within the book |
| `source` | string | event-specific enum | Which surface triggered the event |
| `reason` | string | event-specific enum | Why the event happened (errors, gates) |
| `method` | string | `google` \| `apple` | Auth provider |
| `is_pro` | boolean | `true` | Whether the user has the Pro entitlement |

## Auto-collected events

Firebase automatically logs `first_open`, `app_open`, `session_start`, `user_engagement`, `os_update`, `app_update` and `in_app_purchase` on Android and iOS — do not log these manually. The Desktop target gets **none** of these automatically (Measurement Protocol only sends what we post); this gap is accepted for now since desktop traffic is small.

## destination_view strategy

`destination_view` is not automatic in a Compose Multiplatform single-activity app, so it is emitted centrally by observing the Navigation 3 back stacks:

- the **root** back stack in `core/navigation/.../RootAppNavDisplay.kt`, and
- the **per-tab** back stacks in `feature/main/.../navhost/BottomNavTabState.kt` (each bottom tab owns its own stack).

Whenever the top entry of the visible stack changes, log `destination_view` with the `destination_name` and `destination_type` mapped from the NavKey below. Route arguments become event parameters. Tab clicks are covered by this mechanism, so there is no separate tab-selection event.

Every route (`core/model/.../route/*.kt`) implements the sealed `NavRoute : NavKey` interface. `NavRouteToDestinationMapper.map(route: NavRoute)` switches on it exhaustively — no `else` branch — so adding a new route that forgets to update the mapper is a compile error, not a silent gap. `TrackDestinationUseCase` receives the raw `NavKey` from the back stack (the Navigation 3 API is invariant on `NavKey`) and casts it to `NavRoute` at that single boundary before calling the mapper.

`destination_type` is:

- `screen` for a full NavDisplay entry.
- `dialog` for a route that renders as an `AlertDialog`/`Dialog`/`DatePickerDialog`.
- `bottom_sheet` for a route that renders as a `ModalBottomSheet`.
- `responsive` for a route that renders as a dialog on wide layouts and a bottom sheet on narrow ones, via the shared `ResponsiveDialogSheet` component (`ui/component/.../ResponsiveDialogSheet.kt`) — the actual shape depends on window width at render time, which the NavKey mapping can't see, so these get their own value instead of being forced into `dialog` or `bottom_sheet`.

### NavKey → destination_name / destination_type mapping

| NavKey (`core/model/.../route/`) | `destination_name` | `destination_type` | Route args → params |
|---|---|---|---|
| `MainNavRoute` | *(not logged — shell container; tabs log instead)* | — | — |
| `MainNavRouteDestination.Plans` | `plans` | `screen` | — |
| `MainNavRouteDestination.Books` | `books` | `screen` | — |
| `MainNavRouteDestination.Profile` | `profile` | `screen` | — |
| `AccountDetailsNavRoute` | `account_details` | `responsive` | — |
| `AddNotesFreeWarningNavRoute` | `add_notes_free_warning` | `dialog` | `max_free_notes` |
| `AppLanguageNavRoute` | `app_language` | `responsive` | — |
| `BibleVersionSelectorRoute` | `bible_version_selector` | `responsive` | — |
| `BookDetailsNavRoute` | `book_details` | `screen` | `book_id` |
| `CongratsNavRoute` | `congrats` | `bottom_sheet` | — |
| `ContactSupportNavRoute` | `contact_support` | `responsive` | — |
| `CropPhotoNavRoute` | `crop_profile_photo` | `screen` | — |
| `DayNavRoute` | `day` | `screen` | `plan_type`, `week_number`, `day_number` |
| `DayStudyNavRoute` | `day_study` | `screen` | `plan_type`, `week_number`, `day_number` |
| `DeleteAllProgressNavRoute` | `delete_all_progress` | `dialog` | — |
| `DeleteNotesRoute` | `delete_notes` | `dialog` | `plan_type`, `week_number`, `day_number` |
| `DeleteVersionNavRoute` | `delete_version` | `dialog` | `version_id` |
| `DonationNavRoute` | `donation` | `bottom_sheet` | — |
| `EditNameNavRoute` | `edit_profile_name` | `dialog` | — |
| `EditPhotoSourceNavRoute` | `edit_profile_photo_source` | `responsive` | — |
| `EditPlanStartDateNavRoute` | `edit_plan_start_date` | `dialog` | — |
| `EditProfileNavRoute` | `edit_profile` | `responsive` | — |
| `ExpandedPhotoNavRoute` | `profile_photo_expanded` | `dialog` | — |
| `InAppUpdateNavRoute` | `in_app_update` | `responsive` | — |
| `LoginNavRoute` | `login` | `bottom_sheet` | — |
| `LoginSyncNudgeNavRoute` | `login_sync_nudge` | `dialog` | — |
| `LoginWarningNavRoute` | `login_warning` | `dialog` | `reason` |
| `LogoutNavRoute` | `logout` | `dialog` | — |
| `MaterialYouBottomSheetNavRoute` | `material_you` | `dialog` | — |
| `NotificationPermissionNavRoute` | `notification_permission` | `dialog` | — |
| `PaywallNavRoute` | `paywall` | `screen` | — |
| `PixQrNavRoute` | `pix_qr` | `dialog` | — |
| `ReadNavRoute` | `read` | `screen` | `book_id`, `chapter_number` |
| `ReleaseNotesNavRoute` | `release_notes` | `screen` | — |
| `RenameDeviceNavRoute` | `rename_device` | `dialog` | — |
| `SubscriptionDetailsNavRoute` | `subscription_details` | `dialog` | — |
| `ThemeNavRoute` | `theme_selection` | `responsive` | — |
| `UpdateDownloadedNavRoute` | `update_downloaded` | `dialog` | — |

`InAppUpdateNavRoute` carries `version_name` and `source` args, but — like `LoginNavRoute` — they are not mapped onto `destination_view`; the richer funnel parameters live on the dedicated [update_prompt_shown](events/update_prompt_shown.md) event instead.

`MaterialYouBottomSheetNavRoute` is named after an earlier bottom-sheet implementation but currently renders as a centered `Dialog` (`feature/material_you/.../MaterialYouDialog.kt`) — classified by actual rendering, not by route name.

The authoritative NavKey registry is `core/model/.../route/Nav3SavedStateConfiguration.kt`; keep this table in sync when routes are added. Since `NavRouteToDestinationMapper` is an exhaustive `when` over the sealed `NavRoute`, forgetting to add a new route there fails the build immediately rather than silently missing a `destination_view`.

## User properties

| Property | Status | Source |
|---|---|---|
| `is_tester` | existing | auth user id vs Remote Config `tester_user_ids` allowlist |
| `is_pro` | proposed | `ObserveIsProUser` (RevenueCat entitlement) |
| `app_language` | proposed | `GetAppLanguageFlow` |
| `app_theme` | proposed | `GetThemeOptionFlow` |
| `selected_plan_type` | proposed | active reading plan preference |

Setting `user_id` to the Supabase user id would allow cross-referencing with RevenueCat, but is an **open question pending an LGPD/consent review** — not decided here.

## Tiers

- **P1** — business funnels: reading progress, monetization, auth, settings that shape retention. Implement first.
- **P2** — UI micro-interactions: filters, sorting, expand/collapse, shortcuts. Implement in a later phase.

## Event index

### Destination tracking

| Event | Tier | Domain |
|---|---|---|
| [destination_view](events/destination_view.md) | P1 | Navigation |

### Reading

| Event | Tier | Domain |
|---|---|---|
| [chapter_read_toggled](events/chapter_read_toggled.md) | P1 | Reading |
| [day_read_toggled](events/day_read_toggled.md) | P1 | Reading |
| [book_read_toggled](events/book_read_toggled.md) | P1 | Reading |
| [book_completed](events/book_completed.md) | P1 | Reading |
| [week_completed](events/week_completed.md) | P1 | Reading |
| [plan_completed](events/plan_completed.md) | P1 | Reading |
| [bible_progress_milestone](events/bible_progress_milestone.md) | P2 | Reading |
| [reading_streak_milestone](events/reading_streak_milestone.md) | P2 | Reading |
| [in_app_review_requested](events/in_app_review_requested.md) | P2 | Reading |
| [book_favorite_toggled](events/book_favorite_toggled.md) | P1 | Reading |
| [plan_selected](events/plan_selected.md) | P1 | Reading |
| [plan_start_date_changed](events/plan_start_date_changed.md) | P1 | Reading |
| [read_date_edited](events/read_date_edited.md) | P2 | Reading |
| [progress_reset_confirmed](events/progress_reset_confirmed.md) | P1 | Reading |
| [progress_reset_cancelled](events/progress_reset_cancelled.md) | P2 | Reading |
| [reading_suggestion_clicked](events/reading_suggestion_clicked.md) | P2 | Reading |
| [day_card_clicked](events/day_card_clicked.md) | P2 | Reading |
| [read_back_clicked](events/read_back_clicked.md) | P2 | Reading |
| [book_clicked](events/book_clicked.md) | P2 | Reading |
| [book_details_back_clicked](events/book_details_back_clicked.md) | P2 | Reading |
| [chapter_clicked](events/chapter_clicked.md) | P2 | Reading |
| [day_back_clicked](events/day_back_clicked.md) | P2 | Reading |
| [read_retry_clicked](events/read_retry_clicked.md) | P2 | Reading |
| [day_edit_date_clicked](events/day_edit_date_clicked.md) | P2 | Reading |
| [day_time_picker_shown](events/day_time_picker_shown.md) | P2 | Reading |
| [day_date_picker_dismissed](events/day_date_picker_dismissed.md) | P2 | Reading |
| [day_date_selected](events/day_date_selected.md) | P2 | Reading |

### Notes

| Event | Tier | Domain |
|---|---|---|
| [note_saved](events/note_saved.md) | P1 | Notes |
| [note_deleted](events/note_deleted.md) | P1 | Notes |
| [note_delete_cancelled](events/note_delete_cancelled.md) | P2 | Notes |
| [notes_limit_reached](events/notes_limit_reached.md) | P1 | Notes |
| [add_notes_free_warning_dismissed](events/add_notes_free_warning_dismissed.md) | P2 | Notes |
| [notes_clear_clicked](events/notes_clear_clicked.md) | P2 | Notes |

### Day Study (AI)

| Event | Tier | Domain |
|---|---|---|
| [day_study_card_clicked](events/day_study_card_clicked.md) | P1 | DayStudy |
| [day_study_generation_started](events/day_study_generation_started.md) | P1 | DayStudy |
| [day_study_generation_completed](events/day_study_generation_completed.md) | P1 | DayStudy |
| [day_study_generation_failed](events/day_study_generation_failed.md) | P1 | DayStudy |
| [day_study_opened](events/day_study_opened.md) | P2 | DayStudy |
| [day_study_login_required_clicked](events/day_study_login_required_clicked.md) | P2 | DayStudy |
| [day_study_retry_clicked](events/day_study_retry_clicked.md) | P2 | DayStudy |
| [day_study_load](events/day_study_load.md) | P1 | DayStudy |
| [day_study_generation_time](events/day_study_generation_time.md) | P1 | DayStudy |

### Bible versions

| Event | Tier | Domain |
|---|---|---|
| [bible_version_selected](events/bible_version_selected.md) | P1 | BibleVersions |
| [bible_version_download_started](events/bible_version_download_started.md) | P1 | BibleVersions |
| [bible_version_download_completed](events/bible_version_download_completed.md) | P1 | BibleVersions |
| [bible_version_download_failed](events/bible_version_download_failed.md) | P1 | BibleVersions |
| [bible_version_download_paused](events/bible_version_download_paused.md) | P2 | BibleVersions |
| [bible_version_deleted](events/bible_version_deleted.md) | P2 | BibleVersions |
| [bible_version_delete_cancelled](events/bible_version_delete_cancelled.md) | P2 | BibleVersions |
| [bible_version_manage_clicked](events/bible_version_manage_clicked.md) | P2 | BibleVersions |
| [bible_version_delete_clicked](events/bible_version_delete_clicked.md) | P2 | BibleVersions |
| [bible_version_manager_dismissed](events/bible_version_manager_dismissed.md) | P2 | BibleVersions |
| [bible_version_download_retry_clicked](events/bible_version_download_retry_clicked.md) | P2 | BibleVersions |

### Monetization

| Event | Tier | Domain |
|---|---|---|
| [paywall_viewed](events/paywall_viewed.md) | P1 | Monetization |
| [paywall_plan_selected](events/paywall_plan_selected.md) | P1 | Monetization |
| [purchase_started](events/purchase_started.md) | P1 | Monetization |
| [purchase_completed](events/purchase_completed.md) | P1 | Monetization |
| [purchase_failed](events/purchase_failed.md) | P1 | Monetization |
| [restore_completed](events/restore_completed.md) | P1 | Monetization |
| [restore_failed](events/restore_failed.md) | P1 | Monetization |
| [paywall_dismissed](events/paywall_dismissed.md) | P2 | Monetization |
| [donation_method_copied](events/donation_method_copied.md) | P1 | Monetization |
| [github_sponsors_opened](events/github_sponsors_opened.md) | P1 | Monetization |
| [donation_section_toggled](events/donation_section_toggled.md) | P2 | Monetization |
| [pix_qr_shared](events/pix_qr_shared.md) | P1 | Monetization |
| [congrats_dismissed](events/congrats_dismissed.md) | P2 | Monetization |
| [congrats_explore_clicked](events/congrats_explore_clicked.md) | P2 | Monetization |
| [donation_dismissed](events/donation_dismissed.md) | P2 | Monetization |
| [pix_qr_opened](events/pix_qr_opened.md) | P2 | Monetization |
| [pix_qr_dismissed](events/pix_qr_dismissed.md) | P2 | Monetization |

### Auth & sync

| Event | Tier | Domain |
|---|---|---|
| [login](events/login.md) | P1 | Auth |
| [login_started](events/login_started.md) | P1 | Auth |
| [login_failed](events/login_failed.md) | P1 | Auth |
| [login_cancelled](events/login_cancelled.md) | P2 | Auth |
| [google_account_add_confirmed](events/google_account_add_confirmed.md) | P2 | Auth |
| [google_account_add_declined](events/google_account_add_declined.md) | P2 | Auth |
| [login_sheet_dismissed](events/login_sheet_dismissed.md) | P2 | Auth |
| [logout_confirmed](events/logout_confirmed.md) | P1 | Auth |
| [logout_cancelled](events/logout_cancelled.md) | P2 | Auth |
| [logout_failed](events/logout_failed.md) | P1 | Auth |
| [login_nudge_shown](events/login_nudge_shown.md) | P1 | Auth |
| [login_nudge_accepted](events/login_nudge_accepted.md) | P1 | Auth |
| [login_nudge_snoozed](events/login_nudge_snoozed.md) | P2 | Auth |
| [login_nudge_disabled](events/login_nudge_disabled.md) | P1 | Auth |
| [login_nudge_dont_show_again_toggled](events/login_nudge_dont_show_again_toggled.md) | P2 | Auth |
| [login_warning_shown](events/login_warning_shown.md) | P1 | Auth |
| [login_warning_accepted](events/login_warning_accepted.md) | P1 | Auth |
| [login_warning_dismissed](events/login_warning_dismissed.md) | P2 | Auth |
| [sync_completed](events/sync_completed.md) | P2 | Auth |
| [sync_failed](events/sync_failed.md) | P2 | Auth |
| [session_lost](events/session_lost.md) | P1 | Auth |
| [current_device_revoked](events/current_device_revoked.md) | P1 | Auth |
| [rename_device_clicked](events/rename_device_clicked.md) | P2 | Auth |

### Settings & shell

| Event | Tier | Domain |
|---|---|---|
| [profile_option_clicked](events/profile_option_clicked.md) | P1 | Settings |
| [edit_profile_clicked](events/edit_profile_clicked.md) | P2 | Settings |
| [profile_avatar_clicked](events/profile_avatar_clicked.md) | P2 | Settings |
| [edit_profile_name_clicked](events/edit_profile_name_clicked.md) | P2 | Settings |
| [edit_profile_photo_clicked](events/edit_profile_photo_clicked.md) | P2 | Settings |
| [profile_name_updated](events/profile_name_updated.md) | P1 | Settings |
| [profile_photo_source_selected](events/profile_photo_source_selected.md) | P1 | Settings |
| [profile_photo_removed](events/profile_photo_removed.md) | P2 | Settings |
| [profile_photo_updated](events/profile_photo_updated.md) | P1 | Settings |
| [profile_photo_crop_cancelled](events/profile_photo_crop_cancelled.md) | P2 | Settings |
| [theme_changed](events/theme_changed.md) | P1 | Settings |
| [contrast_changed](events/contrast_changed.md) | P2 | Settings |
| [dynamic_colors_toggled](events/dynamic_colors_toggled.md) | P2 | Settings |
| [setting_sync_toggled](events/setting_sync_toggled.md) | P2 | Settings |
| [language_changed](events/language_changed.md) | P1 | Settings |
| [notification_permission_result](events/notification_permission_result.md) | P1 | Settings |
| [notification_permission_prompted](events/notification_permission_prompted.md) | P2 | Settings |
| [notification_permission_declined](events/notification_permission_declined.md) | P2 | Settings |
| [notification_opened](events/notification_opened.md) | P2 | Settings |
| [release_notes_tab_selected](events/release_notes_tab_selected.md) | P2 | Settings |
| [github_release_opened](events/github_release_opened.md) | P2 | Settings |
| [contact_support_email_opened](events/contact_support_email_opened.md) | P1 | Settings |
| [contact_support_email_copied](events/contact_support_email_copied.md) | P2 | Settings |
| [contact_support_dismissed](events/contact_support_dismissed.md) | P2 | Settings |
| [release_notes_back_clicked](events/release_notes_back_clicked.md) | P2 | Settings |
| [theme_selection_dismissed](events/theme_selection_dismissed.md) | P2 | Settings |
| [material_you_info_clicked](events/material_you_info_clicked.md) | P2 | Settings |
| [material_you_info_dismissed](events/material_you_info_dismissed.md) | P2 | Settings |
| [material_you_got_it_clicked](events/material_you_got_it_clicked.md) | P2 | Settings |
| [sync_toggle_blocked_clicked](events/sync_toggle_blocked_clicked.md) | P2 | Settings |
| [app_language_dismissed](events/app_language_dismissed.md) | P2 | Settings |
| [edit_plan_start_date_dismissed](events/edit_plan_start_date_dismissed.md) | P2 | Settings |
| [pro_card_clicked](events/pro_card_clicked.md) | P2 | Settings |
| [account_card_clicked](events/account_card_clicked.md) | P2 | Settings |
| [login_row_clicked](events/login_row_clicked.md) | P2 | Settings |
| [logout_clicked](events/logout_clicked.md) | P2 | Settings |
| [bottom_tab_clicked](events/bottom_tab_clicked.md) | P2 | Settings |

### App updates

| Event | Tier | Domain |
|---|---|---|
| [update_prompt_shown](events/update_prompt_shown.md) | P1 | Updates |
| [update_accepted](events/update_accepted.md) | P1 | Updates |
| [update_dismissed](events/update_dismissed.md) | P2 | Updates |
| [update_download_failed](events/update_download_failed.md) | P2 | Updates |
| [update_install_started](events/update_install_started.md) | P2 | Updates |
| [update_install_postponed](events/update_install_postponed.md) | P2 | Updates |

### Books browsing UI

| Event | Tier | Domain |
|---|---|---|
| [books_search_used](events/books_search_used.md) | P2 | Books |
| [books_filter_toggled](events/books_filter_toggled.md) | P2 | Books |
| [books_sort_changed](events/books_sort_changed.md) | P2 | Books |
| [books_layout_changed](events/books_layout_changed.md) | P2 | Books |
| [books_filter_menu_opened](events/books_filter_menu_opened.md) | P2 | Books |
| [books_filter_menu_dismissed](events/books_filter_menu_dismissed.md) | P2 | Books |
| [books_sort_menu_opened](events/books_sort_menu_opened.md) | P2 | Books |
| [books_sort_menu_dismissed](events/books_sort_menu_dismissed.md) | P2 | Books |
| [books_search_cleared](events/books_search_cleared.md) | P2 | Books |
| [testament_switched](events/testament_switched.md) | P2 | Books |
| [web_app_link_opened](events/web_app_link_opened.md) | P2 | Books |
| [synopsis_toggled](events/synopsis_toggled.md) | P2 | Books |
| [plan_week_toggled](events/plan_week_toggled.md) | P2 | Books |
| [plan_group_toggled](events/plan_group_toggled.md) | P2 | Books |
| [plan_shortcut_used](events/plan_shortcut_used.md) | P2 | Books |
| [plan_overflow_option_clicked](events/plan_overflow_option_clicked.md) | P2 | Books |
| [plan_edit_clicked](events/plan_edit_clicked.md) | P2 | Books |
| [plan_overflow_clicked](events/plan_overflow_clicked.md) | P2 | Books |
| [plan_overflow_dismissed](events/plan_overflow_dismissed.md) | P2 | Books |

## Explicitly not tracked

These interactions were reviewed and deliberately left out — not user actions at all, so there is
no click to attribute (see "Every click gets its own event" above for what *is* tracked, including
menu open/close, retries, and picker steps, which all have dedicated events despite also being
low-signal-sounding names):

- Scroll bookkeeping (`OnScrollStateChange`, `OnActiveRowVisibilityChange`, etc.) — continuous
  signals fired during a gesture, not a discrete action.
- Animation/scroll-completion callbacks (`OnScrollToWeekCompleted`, `OnScrollToTopCompleted`,
  `OnFlashCompleted`) — fired when a programmatic animation finishes, not user-initiated.
- VM lifecycle hooks not triggered by the user (`DayStudyUiEvent.OnStart`) and incoming
  messages/snackbar bridges (`DayUiEvent.OnDayStudyMessage`).

## Adding a new event

Prefer running the `add-analytics-event` skill, which walks these steps:

1. Decide whether the action needs an event at all. Every click gets its own event — even one that only opens a screen/dialog/sheet, since `destination_view` measures screen-view rate, not click-through rate. Only genuine UI plumbing (see "Explicitly not tracked" above) is exempt, classified as `NotTracked`. A business action — including navigation — should never end up `NotTracked`.
2. Add the event name as a `const val` in `AnalyticsEventNames.kt` and any new parameter keys in `AnalyticsParams.kt`.
3. Classify the `UiEvent` case: `Track.Automatic(name, params)` for static params (auto-emitted), or `Track.Manual(names)` (keep the `trackEvent(...)` call in the branch) for params only known post-domain / from `UiState`.
4. Create `events/<event_name>.md` following the template used by the existing files (Tier/Domain header, When it fires, Trigger source, Parameters, Notes).
5. Add the event to the index table above, in its domain section.
6. Run `:core:provider:analytics:jvmTest` — `AnalyticsCatalogTest` fails if a `Track.Manual` name isn't actually wired to a `trackEvent` call, or if any declared name is missing its catalog entry.
