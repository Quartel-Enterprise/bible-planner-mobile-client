package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/*
 * Why: a native main screen (Android) still draws the tabs that have no native version yet with their shared
 * entries, and only the composition root can build them with the root's shared transition scope.
 */
val LocalMainTabEntries = staticCompositionLocalOf<MainTabEntries?> { null }
