package com.quare.bibleplanner.android.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.staticCompositionLocalOf

/*
 * Why: a native tab sits in the main screen's inner NavDisplay, whose LocalNavAnimatedContentScope animates tab
 * switches. Shared elements toward a root screen (the day) need the main entry's own scope, which the main screen
 * provides here, as it passes it to the shared tabs.
 */
val LocalMainAnimatedContentScope = staticCompositionLocalOf<AnimatedContentScope?> { null }
