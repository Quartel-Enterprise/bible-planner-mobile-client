---
name: project-redundant-optin-cleanup
description: "Oct/26 PR #550 removed 131 redundant @OptIn (all ExperimentalSharedTransitionApi, now stable); method = strip all, compile every target, restore; warning-level markers with custom messages are easy to miss"
metadata:
  node_type: memory
  type: project
  originSessionId: 0d84701e-ab3e-448d-885c-94e63e84e706
  modified: 2026-10-04T04:27:21.146Z
---

PR #550 (2026-10-04) removed 131 of 319 `@OptIn` markers that the IDE flagged as redundant. All `ExperimentalSharedTransitionApi` markers went, because the API is stable in CMP 1.12. 36 `ExperimentalMaterial3Api` markers went too.

Method (the compiler never reports a redundant opt-in, only the IDE does):
1. Blank every `@OptIn` line so line numbers stay put.
2. Compile all targets: jvm main and test, `compileAndroidMain`/`HostTest`/`DeviceTest`, iosSimulatorArm64 main and test, wasmJs main and test.
3. Restore the opt-ins the diagnostics point at, and repeat until a round restores nothing.

**Trap:** `@RequiresOptIn` with a custom message or WARNING level doesn't say "opt-in". Examples:
- `FlowPreview`: "in a preview state"
- Calf: "This Calf UI API is experimental"
- Material3: "This material API is experimental"
- compose test: "This testing API is experimental"

**How to apply:** after any strip, review every warning in the changed files, not just the lines that mention opt-in. Both reviews (code-review and review-conventions) caught misses here.

Also noted, but outside this PR's scope:
- main already has many missing-opt-in warnings (`ExperimentalCoroutinesApi` in tests, `FlowPreview` in `StudyFunctionClient`, Calf in `AppSwitch.ios.kt`).
- iOS and wasm **test** compilations are already broken on main: backtick names with commas, `replaceAll`/`synchronized` not available on native, missing wasm actual in `:shared`. CI doesn't compile them.
