package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel.ReleaseNotesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReleaseNotesRoot(
    openUrl: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: ReleaseNotesViewModel = koinViewModel(),
) {
    ReleaseNotesStateLayout(
        viewModel = viewModel,
        openUrl = openUrl,
    ) { uiState ->
        ReleaseNotesScreen(
            platform = viewModel.platform,
            uiState = uiState,
            onEvent = viewModel::onEvent,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
}
