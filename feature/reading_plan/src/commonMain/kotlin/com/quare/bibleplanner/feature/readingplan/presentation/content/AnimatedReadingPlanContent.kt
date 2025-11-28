package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

@Composable
internal fun AnimatedReadingPlanContent(
    modifier: Modifier = Modifier,
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = uiState,
        transitionSpec = { toTransition() },
        label = "reading_plan_state_transition"
    ) { state ->
        when (state) {
            is ReadingPlanUiState.Loaded -> LoadedReadingPlanContent(
                data = state.data,
                onEvent = onEvent
            )

            ReadingPlanUiState.Loading -> LoadingReadingPlanContent()
        }
    }
}

private fun AnimatedContentTransitionScope<ReadingPlanUiState>.toTransition(): ContentTransform = when {
    targetState is ReadingPlanUiState.Loaded && initialState is ReadingPlanUiState.Loading -> {
        fadeIn(animationSpec = tween(600)) +
            slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(600)
            ) togetherWith
            fadeOut(animationSpec = tween(300)) +
            slideOutVertically(
                targetOffsetY = { -it / 2 },
                animationSpec = tween(300)
            )
    }

    else -> {
        fadeIn(animationSpec = tween(300)) +
            slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = tween(300)
            ) togetherWith
            fadeOut(animationSpec = tween(600)) +
            slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(600)
            )
    }
}
