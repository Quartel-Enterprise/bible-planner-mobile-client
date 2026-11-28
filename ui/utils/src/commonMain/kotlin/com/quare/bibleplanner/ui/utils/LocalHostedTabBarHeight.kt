package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp

/*
 * Why: the native Android main screen draws the tab bar or rail itself around the shared tabs. It provides the
 * height its bottom bar covers (zero beside a rail), and MainTabScaffold then draws only the tab's body. Null,
 * as on iOS and the web, keeps the bar or rail inside each tab.
 */
val LocalHostedTabBarHeight = compositionLocalOf<Dp?> { null }
