---
name: navigation3-migration
description: App fully migrated to Navigation 3 (no Nav2); key API/version facts for the multiplatform artifacts and pending web browser-history integration
metadata: 
  node_type: memory
  type: project
  originSessionId: 19beac0b-f82c-4e4d-a15c-e464370510d3
---

Branch `spike/navigation-3` (2026-07-09) fully migrated the app to Navigation 3 and removed `navigation-compose`. Facts not obvious from the code:

- Multiplatform artifact is `org.jetbrains.androidx.navigation3:navigation3-ui` (stable 1.1.x); it pulls `androidx.navigation3:navigation3-runtime` (Google-published KMP, has `NavKey`) transitively. A `navigation3-ui-wasm-js` variant exists for the future web target.
- The stable 1.1.x DSL class is `EntryProviderScope` (member fun `entry<K>`, do not import it); Google's nav3-recipes repo tracks 1.2.0-alpha where it's called `EntryProviderBuilder` — adapt recipes accordingly.
- Non-JVM targets require `rememberNavBackStack(configuration, ...)` with a polymorphic `SerializersModule` registering every `NavKey` subclass — centralized in `core/model/.../route/Nav3SavedStateConfiguration.kt`; forgetting to register a new route only fails at runtime state-save.
- Nav3 browser-history integration for web is NOT yet in the base library (tracked as JetBrains CMP-8924; PoC lib `terrakok/navigation3-browser`) — check status before adding the wasmJs target.
- `popUpTo(current){inclusive=true}` patterns all mapped to a single `onNavigateReplacingTop` lambda (pop+push) in [[navigation3-migration]] convention; docs/architecture/navigation.md documents the lambda contract.
- 2026-07-09 follow-up: narrowed every navigation lambda/field from `Any` to `NavKey` (`onNavigate`, `onNavigateReplacingTop`, `route: Any` fields on UiAction/UiEvent, `NavigationEventBus`, `BottomNavigationItemModel<T : Any>` → `<T : NavKey>`), removing the unsafe `route as NavKey` casts in `RootAppNavDisplay.kt`. `docs/architecture/navigation.md` updated to show `(NavKey) -> Unit` as the convention going forward.
