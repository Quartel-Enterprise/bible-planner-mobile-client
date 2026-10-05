---
name: itunes-lookup-cdn-cache
description: "iOS update check (iTunes lookup API) was stale for hours after a release due to Akamai CDN + device URL cache; fixed in PR #543 with a random cache-buster param."
metadata:
  node_type: memory
  type: project
  originSessionId: ec3503ea-73f8-4edf-b6ae-05b0e4752663
  modified: 2026-10-04T00:52:44.604Z
---

The plain `itunes.apple.com/lookup?id=...` URL is served from Apple's Akamai CDN cache for hours after a release (Oct/2026: returned 2.9.2 while 2.10.0 was live) and carries `max-age=86400`, so NSURLSession also caches it on device. PR #543 (merged 2026-10-04) moved the check to commonMain `ItunesCheckForUpdate`, adds a random `t` param, looks up the device-region storefront with fallback to default, and maps failures to `UpdateAvailability.CheckFailed` (snackbar instead of "up to date", Android too).

**Why:** removing the "noise" query param silently brings back the stale-version bug.
**How to apply:** any new App Store/CDN-backed lookup needs a cache-buster; keep `t` in ItunesCheckForUpdate (guarded by ItunesCheckForUpdateTest).
