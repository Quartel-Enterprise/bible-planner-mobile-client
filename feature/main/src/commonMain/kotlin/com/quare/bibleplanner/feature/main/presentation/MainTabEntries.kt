package com.quare.bibleplanner.feature.main.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
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
