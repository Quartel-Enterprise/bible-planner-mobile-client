# language_changed

**Tier:** P1 | **Domain:** Settings

Captures the user switching the app language. Language distribution drives localization priorities (which release-notes and store listings to invest in) and backs the proposed `app_language` user property.

## When it fires

User selects a language in the app-language bottom sheet.

## Trigger source

`feature/preferences/app_language/src/commonMain/kotlin/com/quare/bibleplanner/feature/applanguage/presentation/AppLanguageViewModel.kt` — `AppLanguageUiEvent.OnLanguageSelected(language: Language)`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `language` | string | `pt` | Selected language as ISO 639-1 code: `en` \| `pt` \| `es` (mapped from `Language.ENGLISH` / `PORTUGUESE_BRAZIL` / `SPANISH`) |

## Notes

- Fires on every selection, including re-selecting the current language — instrumentation may skip no-op selections.
- Should also refresh the proposed `app_language` user property (see README).
- Related: [setting_sync_toggled](setting_sync_toggled.md) (`setting=language`); sheet impressions are covered by [destination_view](destination_view.md) (`app_language`).
