package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute

@OptIn(ExperimentalSharedTransitionApi::class)
fun EntryProviderScope<NavKey>.releaseNotes(
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
) {
    entry<ReleaseNotesNavRoute> {
        val uriHandler = LocalUriHandler.current
        ReleaseNotesRoot(
            navigateBack = onNavigateBack,
            openUrl = { url -> uriHandler.openUri(url) },
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = LocalNavAnimatedContentScope.current,
        )
    }
}
