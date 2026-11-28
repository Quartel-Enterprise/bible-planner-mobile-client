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
   web reaches gain a `js` target next to (and later instead of) `wasmJs`. Every multiplatform module now has both
   targets: web code goes in `webMain`, which both share, and `:shared` builds the `shared` JS library with
   TypeScript definitions (`./gradlew :shared:jsBrowserProductionLibraryDistribution`). A module whose build adds its
   own `dependsOn` edges turns off the default hierarchy, so it calls `dependWebTargetsOnWebMain()` (build-logic)
   to wire `jsMain` and `wasmJsMain` to `webMain`. The js target builds ES modules, so `import.meta.url` (the Room
   worker) keeps working once the React app bundles the library.
2. **Android native.** The composables move out of the shared feature modules into Android modules with a design
   system on Material 3 Expressive, laid out like Blitz Split's `android/`:
   - `android/designsystem` holds the theme (`ExpressiveTheme`, Material 3 Expressive over the app's color scheme)
     and the components every screen shares, including the date and time labels (`date` package: medium date, time
     on the system 12/24-hour setting, relative days such as Today or 3 days ago) and their strings.
   - `android/feature/<name>` holds one feature's screen: a `<Name>Content` composable that takes the shared
     ViewModel's state and `onEvent`, its strings in Android `res/values*/strings.xml` (`R.string`), and a
     `<Route>.toNavEntry()` that builds the Navigation 3 entry with the shared ViewModel from Koin.
     Book content is the exception: book names, book group titles and synopses (66 books in every language) stay in
     the shared Compose resources, and native screens read them from there until step 4 gives them one native home.
   - Every module applies the `bibleplanner.android.compose` convention plugin, which adds Compose and the Material
     icons, and to every feature module Koin, Navigation 3, the design system and `android/navigation`. Material 3 comes from the androidx artifact, the only one whose Expressive APIs are
     public; Compose Multiplatform's Android build already depends on it, so both resolve to one library.
   - Each feature module registers itself, so screens can land in parallel pull requests without touching a shared
     file: `settings.gradle.kts` includes every directory under `android/feature`, `:androidApp` depends on all of
     them, and the module lists its `NativeEntryFactory` (`android/navigation`, the class that turns its route into
     `toNavEntry()`) in `src/main/resources/META-INF/services/com.quare.bibleplanner.android.navigation.NativeEntryFactory`.
     The convention plugin fails the build when that file is missing or lists no factory from the module's own package.
   - `:androidApp` binds a `NativeEntryProvider` (`:core:navigation`) that asks the factories `ServiceLoader` finds;
     `RootAppNavDisplay` asks it first and falls back to the shared entry, so screens move one pull request at a
     time and the Compose Multiplatform version stays for iOS and the web until step 4. The shared ViewModel
     becomes `public` with an `internal` constructor so the Android module can name it.
   - The tabs follow the same rule one level down: `android/feature/main` draws the tab bar or rail and runs the
     inner `NavDisplay`, which asks the `NativeEntryProvider` for each tab key and falls back to the shared tab
     from `LocalMainTabEntries` (provided by `RootAppNavDisplay` with the root's shared transition scope). It
     provides `LocalHostedTabBarHeight`, so the shared `MainTabScaffold` draws only the tab's body there. On narrow
     screens the bar hides on scroll like the shared one: the tabs draw under it and pad their scrolling content's
     end by `LocalHostedTabBarHeight`, while `LocalHostedTabBarHeightOffset` says how far it slid away, so
     `ReserveHostedTabBarHeightEffect` keeps snackbars and FABs on its visible top. A native
     tab draws only its body too, does the same, and keeps its shared elements toward root screens
     with `LocalSharedTransitionScope` and `LocalMainEntryAnimatedContentScope` (`android/navigation`), the root
     entry's scope that the main screen provides, since the factory only gets the tab's key.
   - `assertModuleGraph` keeps them on top: nothing shared depends on `:android:*`, an Android feature never
     depends on another one, and none depends on `:shared`. jscpd doesn't scan `android/` while its screens
     still duplicate the Compose Multiplatform ones they replace.
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
