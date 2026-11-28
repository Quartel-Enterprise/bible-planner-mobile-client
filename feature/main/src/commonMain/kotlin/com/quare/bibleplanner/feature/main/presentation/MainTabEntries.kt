package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun interface MainTabEntries {
    fun register(
        scope: EntryProviderScope<NavKey>,
        navigationBar: @Composable (Modifier) -> Unit,
        navigationRail: @Composable () -> Unit,
        animatedContentScope: AnimatedContentScope,
    )
}

/*
 * Why: a native main screen (Android) still draws the tabs that have no native version yet with their shared
 * entries, and only the composition root can build them with the root's shared transition scope.
 */
val LocalMainTabEntries = staticCompositionLocalOf<MainTabEntries?> { null }
