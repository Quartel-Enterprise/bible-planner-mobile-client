---
name: project-calf-native-tab-bar
description: "Calf (com.mohamedrejeb.calf:calf-ui 0.14.0) AdaptiveNavigationBar gives iOS a native UITabBar in feature/main; gotchas it forced (material3 alpha, #530 overlay, ignored modifier, theme sync)."
metadata: 
  node_type: memory
  type: project
  originSessionId: d55db425-3d69-41d4-8c76-3cdde6ff3732
  modified: 2026-09-14T00:56:36.890Z
---

Branch `feat/calf-adaptive-navigation-bar` (2026-09-13): the main tab bar uses Calf `AdaptiveNavigationBar` (native UITabBar / Liquid Glass on iOS, Material NavigationBar elsewhere; the rail stays Material).

- calf-ui 0.14.0 transitively pulls `compose-material3` 1.12.0-alpha03 over the app's 1.9.0; the user chose to accept it, with a comment on the catalog `calf` line.
- The UITabBar is a subview of the root UIViewController, so it floats over Compose content. Calf issue #530: it stays visible over pushed screens and during back gestures. The workaround is `MainNavigationBar(isNativeBarVisible = !isNativeNavigationBar || rootTransition.isSettled())`, which swaps in a same-height Spacer while hidden.
- On iOS Calf ignores `modifier`, so MainNavigationBar wraps it in a Box. `ui/utils` `expect val isNativeNavigationBar` turns off exitAlways scroll-to-hide (`canScroll`) in MainTabScaffold.
- UITabBar follows UIKit traits, not the in-app theme, so MainViewController sets `overrideUserInterfaceStyle` via `App(onThemeResolved)`.
- iOS tab icons are SF Symbols in `MainNavigationIosIcon`. The profile photo can't go through Calf: `UIKitImage.Bitmap` uses scale 1 and UITabBar applies tint (template) to images. `NativeTabBarAvatarEffect.ios.kt` finds the `UITabBar`s in `LocalUIViewController.view.subviews` and overwrites the Profile item's image/selectedImage (circle drawn with UIGraphicsImageRenderer, `AlwaysOriginal`, primary ring when selected; photo via Coil or Pending bytes; restores the SF Symbol when there's no photo). It depends on Calf internals — review it when upgrading Calf.
- Compose bottom sheets and dialogs render above the native bar (verified on iOS 26.5).
