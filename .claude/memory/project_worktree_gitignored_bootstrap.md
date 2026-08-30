---
name: project-worktree-gitignored-bootstrap
description: "A fresh git worktree needs three gitignored files copied from the main checkout — local.properties, androidApp/google-services.json, iosApp/iosApp/GoogleService-Info.plist — before it can build."
metadata: 
  node_type: memory
  type: project
  originSessionId: af03f6c5-fc56-4c0e-b134-4ebadf03db9d
  modified: 2026-08-30T18:26:32.723Z
---

A new worktree under `.claude/worktrees/` does not build until these gitignored files are copied over from the main checkout:

- `local.properties`
- `androidApp/google-services.json`
- `iosApp/iosApp/GoogleService-Info.plist`

**Why:** `local.properties` carries the BuildKonfig secrets (SUPABASE_*, GA_*, RC, Sentry) that several `build.gradle.kts` files read via `rootProject.file("local.properties")`, plus the Android `sdk.dir`. Missing secret keys only log a `⚠️ … not found in local.properties` warning and compile to blank strings, so the build still succeeds but the app is silently unconfigured at runtime. The Google Services files are different: the Gradle plugin hard-fails without `google-services.json`.

**How to apply:** copy all three right after `git worktree add`, then build with `./gradlew -p <worktree> :androidApp:assembleDebug` (see [[project-worktree-gradle-cwd-trap]] for why the `-p` is mandatory). Verified working on 2026-08-30: clean worktree off `origin/main` produced a 36 MB debug APK. Note the repo has no `develop` branch — the default branch is `main`.
