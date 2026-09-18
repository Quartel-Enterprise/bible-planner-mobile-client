---
name: project-context-parameters-rejected
description: "Kotlin 2.4 context parameters were piloted for SharedTransitionScope/AnimatedContentScope (PR #445) and deliberately rejected; keep explicit scope parameters."
metadata: 
  node_type: memory
  type: project
  originSessionId: 72d27d59-751e-418d-b21a-cff91d8d5a3b
  modified: 2026-09-15T01:49:51.691Z
---

Context parameters were piloted on 2026-09-15 for the shared-transition scopes (release notes + profile, PR #445). They compiled and ran on JVM/Android/iOS, but the user closed the PR without merging and chose to keep explicit `sharedTransitionScope`/`animatedContentScope` parameters.

**Why:** the compiler (Kotlin 2.4.20, verified with a probe) gives no warning for an unused context parameter, so implicit dependencies go stale silently up the chain and ktlint can't detect them (no type resolution); context parameters can't have default values, so optional scopes like `ProfileMenuItem`'s `SharedTransitionScope? = null` still need explicit forwarding; passing a context argument explicitly at the call site is still experimental (`-Xexplicit-context-arguments`); and `with(sharedTransitionScope)` is still needed for `sharedElement`, so the gain is only the forwarding lines.

**How to apply:** don't propose migrating scope parameters to context parameters again unless explicit context arguments become stable or the compiler starts warning on unused context parameters. Related Kotlin 2.4 adoptions that did ship: explicit backing fields (PR #443) and removal of stable-API opt-ins (PR #444).
