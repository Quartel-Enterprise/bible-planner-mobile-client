package com.quare.bibleplanner.android.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.staticCompositionLocalOf

/*
 * Why: a native tab is drawn inside the main screen's inner NavDisplay, whose LocalNavAnimatedContentScope only
 * animates tab switches. Its shared elements toward root screens (such as book details) need the scope of the
 * root entry that holds the main screen, which the main screen provides here.
 */
val LocalMainEntryAnimatedContentScope = staticCompositionLocalOf<AnimatedContentScope?> { null }
