## Native UI migration

The app is moving from one Compose Multiplatform UI to a native UI per platform, following the architecture of
[Blitz Split](https://github.com/Quartel-Enterprise/blitz-split-client). Domain, data and the ViewModels stay in
Kotlin Multiplatform; only the drawing changes.

| Platform | Target UI |
| --- | --- |
| Android | Jetpack Compose with Material 3 Expressive, Navigation 3 |
| iOS, iPadOS and macOS | SwiftUI, one XcodeGen project with an iOS and a macOS target |
| Web | Vite, React, TypeScript, Tailwind and shadcn/ui over a JS library built from `:shared` |

Linux and Windows are out of scope for now. Every UI adapts to foldables, dual screens, tablets and iPads. The visible
UI may change wherever it makes the app follow the platform's guidelines (Material, the Human Interface Guidelines,
the web's conventions).

### Steps

The migration is incremental: each step is a series of small pull requests, and the app keeps working on every
platform after each one.

1. **Shared foundation.** ViewModels, `UiState`, `UiEvent` and `UiAction` become plain Kotlin with no Compose type
   (no `StringResource`, `Color` or `ImageVector`: each UI maps an enum to its own text and icon).
   `:core:presentation` holds the bridge Swift and JS use to reach them (`FlowWatcher`, `Watch`, `ViewModelHolder`,
   see Blitz Split's [state management](https://github.com/Quartel-Enterprise/blitz-split-client/blob/main/docs/architecture/state-management.md#the-bridge)).
   Navigation becomes a shared back stack the ViewModels drive, which each UI draws. `:shared` becomes the
   composition root that builds the Apple framework and the JS library. The web needs a `js` target with
   `@JsExport` for that: Kotlin/Wasm can't export generic classes, `fun interface`s or lambdas, so the modules the
   web reaches gain a `js` target next to (and later instead of) `wasmJs`.
2. **Android native.** The composables move out of the shared feature modules into Android modules with a design
   system on Material 3 Expressive.
3. **SwiftUI and React**, one feature at a time, starting with the reading flow (main, reading plan, day, read,
   books). Until a screen is migrated, iOS shows its Compose version. The web switches over once every screen is
   native.
4. **Removing the old UI.** Compose Multiplatform leaves iOS and the web, and the JVM desktop app is deleted in
   favor of the macOS target.

Shared repositories for what Bible Planner and Blitz Split have in common come after step 4.

### Making a screen reachable from Swift

A screen is ready for its SwiftUI version once Swift can create its ViewModel and watch it:

1. The ViewModel is `public` with an `internal constructor`, so Koin keeps building it while its dependencies stay
   `internal`. Its `UiState`, `UiEvent`, `UiAction` and every type they reference become `public` too.
2. The ViewModel exposes `stateWatcher` and `actionWatcher`, `FlowWatcher`s over `uiState` and `uiAction` built with
   `viewModelScope`. The feature module takes `:core:presentation` as an `api` dependency.
3. `:shared` exports the feature module in the `Shared` framework (with every module whose types its `UiState`,
   `UiEvent` or `UiAction` use, such as `:core:model`, so Swift sees them without a module prefix), and `NativeViewModels` (in `iosMain`) gains a
   `create<Screen>()` function returning the screen's `ViewModelHolder`.

Swift then keeps the holder for the screen's lifetime, calls `onEvent` on its ViewModel, and calls `clear()` when the
screen goes away.
