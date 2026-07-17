---
name: project_shimmer_wrapper
description: Shimmer is a ui/component wrapper (Modifier.shimmer()/ShimmerBox) delegating to valentinilk compose-shimmer 1.5.0 stable
metadata: 
  node_type: memory
  type: project
  originSessionId: def50517-d9d9-4d50-9155-deace797e9c9
---

Shimmer effect is exposed app-wide via a wrapper in `ui/component` (`ui/component/.../component/shimmer/Shimmer.kt`): `Modifier.shimmer()` and `ShimmerBox`, delegating to `com.valentinilk.shimmer:compose-shimmer`. The lib dependency lives ONLY in `ui/component` so a future break is a one-file swap — the rest of the app consumes our own `shimmer()`, never the lib directly.

Pinned to **1.5.0 stable** (catalog: `composeShimmer`), bumped from the 1.5.0-beta02 pre-release.

**History:** Compose Multiplatform 1.11 changed non-Android `Shader` from a Skia typealias to a Compose wrapper, crashing the lib's older stable releases (≤1.4.0) on **iOS** with a `ClassCastException` in `ShimmerEffect.draw`. 1.5.0-beta02 shipped a Skiko-internals workaround for 1.11.x. The **1.5.0 stable release (2026-07-09) fixes this properly for both Compose Multiplatform 1.11 and 1.12** — confirmed by maintainer comment on [valentinilk/compose-shimmer#62](https://github.com/valentinilk/compose-shimmer/issues/62) ("Is fixed with release 1.5.0"). No PR #64 dependency needed; the CMP-1.12 workaround it describes turned out unnecessary once 1.5.0 landed.
