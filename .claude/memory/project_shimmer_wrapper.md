---
name: project_shimmer_wrapper
description: Shimmer is a ui/component wrapper (Modifier.shimmer()/ShimmerBox) delegating to valentinilk compose-shimmer beta; why the beta and when PR
metadata: 
  node_type: memory
  type: project
  originSessionId: def50517-d9d9-4d50-9155-deace797e9c9
---

Shimmer effect is exposed app-wide via a wrapper in `ui/component` (`ui/component/.../component/shimmer/Shimmer.kt`): `Modifier.shimmer()` and `ShimmerBox`, delegating to `com.valentinilk.shimmer:compose-shimmer`. The lib dependency lives ONLY in `ui/component` so a future break is a one-file swap — the rest of the app consumes our own `shimmer()`, never the lib directly.

Pinned to **1.5.0-beta02** (catalog: `composeShimmer`). 

**Why the beta, not stable:** Compose Multiplatform 1.11 (we're on 1.11.1) changed non-Android `Shader` from a Skia typealias to a Compose wrapper, crashing the lib's stable releases (≤1.4.0) on **iOS**. 1.5.0-beta02 has a Skiko-internals workaround that fixes 1.11.x.

**How to apply / watch:** When bumping Compose Multiplatform to **1.12+**, the beta02 workaround breaks again — you then need [valentinilk/compose-shimmer PR #64](https://github.com/valentinilk/compose-shimmer/pull/64) (delegates to public `ShaderBrush.transform`), so wait for it to merge + release, or swap the wrapper internals to a hand-rolled shimmer (public `Brush.linearGradient` API, immune to the Skiko churn). Android is unaffected by all this.
