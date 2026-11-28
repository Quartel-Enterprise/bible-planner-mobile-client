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
     `ReserveHostedTabBarHeightEffect` keeps snackbars and FABs on its visible top. A tab that jumps its list in code calls
     `LocalRevealHostedTabBar` to bring the hidden bar back. A native
     tab draws only its body too, does the same, and keeps its shared elements toward root screens
     with `LocalSharedTransitionScope` and `LocalMainEntryAnimatedContentScope` (`android/navigation`), the root
     entry's scope that the main screen provides, since the factory only gets the tab's key.
   - `assertModuleGraph` keeps them on top: nothing shared depends on `:android:*`, an Android feature never
     depends on another one, and none depends on `:shared`. jscpd doesn't scan `android/` while its screens
     still duplicate the Compose Multiplatform ones they replace.
3. **SwiftUI and React**, one feature at a time, starting with the reading flow (main, reading plan, day, read,
   books). Until a screen is migrated, iOS shows its Compose version. The web switches over once every screen is
   native: until then the React app lives in `web/`, next to the Compose one in `webApp/`, which keeps serving
   the site (see [The React web app](#the-react-web-app)).
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

### Making a screen reachable from React

Members of a Kotlin class that isn't `@JsExport`ed get mangled names in the JS output, and a feature module can't
export its types from `commonMain`, since Kotlin/Wasm only exports functions. So React doesn't reach the ViewModel
itself, as Swift does, but a presenter in `:shared`'s `jsMain` (`com.quare.bibleplanner.bridge`, files marked
`@file:JsExport`), like Blitz Split's:

1. The ViewModel is ready for Swift (above): `public` with an `internal constructor`, and a `stateWatcher`.
2. The presenter (`MainPresenter`) builds the screen's `ViewModelHolder` from Koin, the JS analogue of
   `NativeViewModels.create<Screen>()`.
3. It maps the state to small exported classes of plain values (`JsMainTab`): strings, booleans, numbers, arrays
   and nullable values, with enums and routes as string keys. A sealed type, a `List`, a `Long` or a `Flow` never
   crosses into TypeScript.
4. `observe<Something>(onChange)` watches the `stateWatcher` (or `actionWatcher`) and returns a `JsWatch`, since
   `:core:presentation`'s `Watch` is a common `fun interface`. Each `UiEvent` the web sends becomes a method that
   calls `onEvent`, and `dispose()` clears the holder.

`startBiblePlanner()` starts Koin with the web graph (`initializeKoinForWeb()` in `:shared`'s `webMain`, which the
Compose web app's `main()` calls too) before any presenter is created.

### The React web app

`web/` holds the React app: Vite, React and TypeScript (strict), Tailwind with the shadcn/ui tokens filled with the
app's color scheme (light and dark follow `prefers-color-scheme`), shadcn/ui components in `src/components/ui`
(`components.json`, so `npx shadcn add` works), ESLint and Vitest. It depends on the library
`./gradlew :shared:jsBrowserProductionLibraryDistribution` builds in `shared/build/dist/js/productionLibrary`, as
the local package `bibleplanner-shared` (`file:`), with its TypeScript definitions.
Two things in `vite.config.ts` let Vite bundle it: a plugin that points the Room worker URL
(`bibleplanner-sqlite-worker/worker.js`, a local npm package webpack resolves for `webApp`) at
`core/provider/room/web-worker/worker.js`, and an alias of Ktor's Node-only `ws` import to the browser's `WebSocket`.
The app installs `@js-joda/core` and `@sqlite.org/sqlite-wasm` itself, at the versions the library and the worker
use, since npm doesn't install the dependencies of a linked build folder.

A screen holds its presenter with the hooks of `src/bridge/usePresenter.ts`:

```tsx
const presenter = usePresenter(() => new MainPresenter())
const tabs = useWatch(presenter, (p, onChange: (tabs: JsMainTab[]) => void) => p.observeTabs(onChange))
```

`usePresenter` creates the presenter in an effect and disposes it on unmount, so StrictMode's second mount never
reuses a cleared ViewModel; `useWatch` mirrors a flow into React state (`null` until the first value) and cancels
its watch on unmount. The hooks are tested with a fake presenter; the Kotlin side is exercised by the app.

For now the app is a placeholder that lists the main screen's tabs; the screens come feature by feature. See
[getting started](../getting-started.md#build-and-run-the-react-web-app) to run it and [CI](../ci.md#react-web-app)
for its checks.
