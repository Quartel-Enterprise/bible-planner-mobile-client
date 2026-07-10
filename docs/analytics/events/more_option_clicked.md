# more_option_clicked

**Tier:** P1 | **Domain:** Settings

Captures which entry the user taps in the More tab, the app's settings and utilities hub. It reveals which secondary features (donation, web app, release notes, support, etc.) actually get discovered and used, and feeds the entry step of several funnels (monetization via `donate`, support via `contact_support`).

## When it fires

User taps any list item on the More screen.

## Trigger source

`feature/more/src/commonMain/kotlin/com/quare/bibleplanner/feature/more/presentation/viewmodel/MoreViewModel.kt` — `MoreUiEvent.OnItemClick(type: MoreOptionItemType)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `option` | string | `release_notes` | `MoreOptionItemType` value lowercased to snake_case: `become_pro`, `theme`, `app_language`, `instagram`, `privacy_policy`, `terms`, `donate`, `web_app`, `delete_progress`, `edit_plan_start_day`, `release_notes`, `bible_version`, `contact_support`, `rate_app`, `check_for_update` |

## Notes

- The Pro banner card, login and logout rows are separate events on `MoreUiEvent` (`OnProCardClick`, `OnLoginClick`, `OnLogoutClick`) and are covered by the monetization/auth funnels, not by this event.
- The enum value is `EDIT_PLAN_START_DAY` (day, not date), so the param value is `edit_plan_start_day`.
- Destination impressions are also captured by [destination_view](destination_view.md); this event additionally covers options that open external links (`instagram`, `privacy_policy`, `terms`, `web_app`, `rate_app`) with no screen of their own.
