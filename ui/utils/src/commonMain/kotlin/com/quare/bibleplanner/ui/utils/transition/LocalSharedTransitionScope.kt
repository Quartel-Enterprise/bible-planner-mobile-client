package com.quare.bibleplanner.ui.utils.transition

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

/*
 * Why: native screens come from a NativeEntryFactory that only gets the route, so they read the nav display's
 * shared transition scope here to keep their shared elements with the Compose Multiplatform screens.
 */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
