---
name: project-version-catalog-convention
description: "libs.versions.toml follows producer-first kebab-case across versions/libraries/plugins, always module = \"group:artifact\"; standardized 2026-07-27."
metadata: 
  node_type: memory
  type: project
  originSessionId: 94fe98a9-4536-4dc5-b39f-c3845745b0ff
  modified: 2026-07-28T00:05:17.804Z
---

`gradle/libs.versions.toml` was standardized (2026-07-27) following the Gradle recommendations (userguide + the "Best Practices for Naming Version Catalog Entries" blog post):

- **kebab-case** in `[versions]`, `[libraries]` and `[plugins]` — never full-camelCase aliases, never underscores.
- **First segment = producer** (`ktor-`, `koin-`, `compose-`, `supabase-`, `play-`, `navigation3-`). No orphan aliases at the root (`ui`, `runtime`, `foundation` became `compose-ui`, `compose-runtime`, `compose-foundation`).
- **camelCase only to flatten** dashes internal to the artifact id, avoiding extra accessor levels: `ktor-client-contentNegotiation`, `compose-ui-toolingPreview`, `supabase-compose-authUi`, `android-minSdk`.
- **Always `module = "group:artifact"`**, never `group = / name =`.
- `[versions]` aliases carry no `Version` suffix (the section already says it) and share the prefix of the library that uses them.
- A plugin consumed as a library in `build-logic` gets the `-plugin` suffix (`android-gradle-plugin`, `kotlin-gradle-plugin`).
- BOM-managed entries carry no `version.ref` (koin, supabase, firebase).

`android-minSdk` / `android-compileSdk` / `android-targetSdk` stay camelCase on purpose: the Gradle docs recommend it so the accessor doesn't become `libs.versions.android.min.sdk`, and they are read by string in `AndroidSdkVersionsExtension`.

There is no naming lint for TOML (ktlint/Spotless don't cover it — spotless issue #2916 still open). For enforcement the path is the `nl.littlerobots.version-catalog-update` plugin with `versionCatalogFormat` + `git diff --exit-code` in CI — but it sorts alphabetically and destroys the comment-based grouping, so it needs `sortByKey.set(false)`.

See also [[feedback-ktlint-custom-enforcement]].
