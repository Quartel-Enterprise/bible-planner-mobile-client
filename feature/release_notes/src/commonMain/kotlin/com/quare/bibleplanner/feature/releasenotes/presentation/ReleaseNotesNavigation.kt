package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.releaseNotes(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable<ReleaseNotesNavRoute> {
        val uriHandler = LocalUriHandler.current
        ReleaseNotesRoot(
            navigateBack = { navController.navigateUp() },
            openUrl = { url -> uriHandler.openUri(url) },
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this,
        )
    }
}
