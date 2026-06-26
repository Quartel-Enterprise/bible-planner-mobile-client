---
name: project_ios_dsym_unrecoverable
description: iOS dSYMs for shipped builds are NOT downloadable from App Store Connect (bitcode off); must upload to Crashlytics at CI build time.
metadata: 
  node_type: memory
  type: project
  originSessionId: 34ff25a2-4d8f-47d9-b843-6f3fc48b93c1
---

For Bible Planner iOS, a Crashlytics "Missing dSYM" alert for an already-shipped build (e.g. 1.18.0 build 27) is effectively **unrecoverable**. Bitcode is off, so Apple does not regenerate/host dSYMs for download: the App Store Connect internal endpoint `.../trains/<version>/builds/<build>/details` returns `dsymurl: null`, and `fastlane download_dsyms` reads that same field — so it also returns nothing. The web UI no longer has a "Download dSYM" button. The Xcode Organizer only has builds archived locally, and releases are built on ephemeral GitHub Actions runners (IPA artifact retention is 1 day), so the dSYM is gone. Rebuilding does not help — a new build has a different UUID. "Includes Symbols: Yes" in Build Metadata means the binary isn't stripped, NOT that a downloadable dSYM exists.

**Why:** the only reliable copy of a dSYM exists on the CI runner at build time, before it's discarded.

**How to apply:** don't waste time trying to download dSYMs for past builds — confirm `dsymurl` is null and move on. The real fix is to upload dSYMs to Crashlytics during `fastlane ios build` (the gym-produced `build/ios/BiblePlanner.app.dSYM.zip` via `upload_symbols_to_crashlytics`, non-blocking), which covers every build from that release forward. The Xcode "Upload Crashlytics dSYMs" build phase alone is best-effort/silent and can't be trusted. For one-off manual uploads use [[project_crashlytics_integration]] tooling via scripts/upload_dsyms.sh (supports -n/--dry-run with upload-symbols --validate). Validate the binary path with `xcodebuild -resolvePackageDependencies -derivedDataPath build/derived_data`.
