package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable

@Composable
expect fun PlatformReleaseNotesRoot(
    openUrl: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
)
