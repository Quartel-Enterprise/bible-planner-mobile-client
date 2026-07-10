# web_app_link_opened

**Tier:** P2 | **Domain:** Books

Captures the user opening the companion web app from the Books tab. Measures cross-platform interest and whether the web app link placement on the Books screen earns clicks.

## When it fires

User taps the web-app link on the Books tab; the ViewModel resolves the URL and opens it in the browser.

## Trigger source

`feature/books/src/commonMain/kotlin/com/quare/bibleplanner/feature/books/presentation/viewmodel/BooksViewModel.kt` — `BooksUiEvent.OnWebAppLinkClick`

## Parameters

None.

## Notes

- The web app is also reachable from the More tab — that path is captured by [more_option_clicked](more_option_clicked.md) with `option=web_app`, not by this event. Sum both to get total web-app referrals.
