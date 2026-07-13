# app_language_dismissed

**Tier:** P2 | **Domain:** Settings

Captures the user closing the app-language sheet without necessarily changing anything. Complements [language_changed](language_changed.md) to show how often the sheet is opened and dismissed without a selection.

## When it fires

User dismisses the app-language sheet (scrim tap, close action, or system back).

## Trigger source

`feature/preferences/app_language/src/commonMain/kotlin/com/quare/bibleplanner/feature/applanguage/presentation/AppLanguageViewModel.kt` — `AppLanguageUiEvent.OnDismiss`

## Parameters

None.

## Notes

- Destination impression for the sheet itself is covered by [destination_view](destination_view.md) (`app_language`, `responsive`).
- Related: [language_changed](language_changed.md).
