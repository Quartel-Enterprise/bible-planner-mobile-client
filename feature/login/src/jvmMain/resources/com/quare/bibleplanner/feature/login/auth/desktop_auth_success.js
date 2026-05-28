(function () {
    // Strip any tokens / query params from the address bar.
    try {
        window.history.replaceState({}, document.title, location.pathname);
    } catch (_) { /* ignored */ }

    // Try to close the tab. Browsers only honor this for script-opened tabs,
    // which is normally the case for OAuth tabs spawned by Desktop.browse(...).
    setTimeout(function () {
        try { window.close(); } catch (_) { /* ignored */ }
    }, 800);
})();
