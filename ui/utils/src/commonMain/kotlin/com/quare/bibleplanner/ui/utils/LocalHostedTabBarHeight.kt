package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

/*
 * Why: the native Android main screen draws the tab bar or rail itself around the shared tabs. It provides the
 * full height of its bottom bar (zero beside a rail), and MainTabScaffold then draws only the tab's body, which
 * scrolls under the bar and pads its end by that height. Null, as on iOS and the web, keeps the bar or rail
 * inside each tab.
 */
val LocalHostedTabBarHeight = compositionLocalOf<Dp?> { null }

/*
 * Why: the host's bar hides on scroll like NarrowTabScaffold's. This reads how far it slid down, in pixels (zero or
 * negative, like BottomAppBarState.heightOffset), from a snapshot state, so overlays and FABs that read it in a
 * layout, draw or snapshotFlow lambda follow the bar without recomposing the tab on every scroll frame.
 */
val LocalHostedTabBarHeightOffset = staticCompositionLocalOf<() -> Float> { { 0f } }

/*
 * Why: the host's bar only follows the scroll a user drags, so a tab that jumps its list in code (scroll to top, to a
 * week) calls this to bring the hidden bar back. It does nothing where no host provides it, as on iOS and the web.
 */
val LocalRevealHostedTabBar = staticCompositionLocalOf<() -> Unit> { {} }
