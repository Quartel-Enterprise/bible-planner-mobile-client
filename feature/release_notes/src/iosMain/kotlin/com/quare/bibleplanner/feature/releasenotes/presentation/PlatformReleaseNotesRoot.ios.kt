package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.releasenotes.presentation.nativescreen.ReleaseNotesNativeRoot

/*
 * Why: spike of a SwiftUI screen behind the shared ViewModel; the shared-element title from the
 * Profile card is dropped because the native view cannot join a Compose shared transition.
 */
@Composable
actual fun PlatformReleaseNotesRoot(
    openUrl: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    ReleaseNotesNativeRoot(openUrl = openUrl)
}
