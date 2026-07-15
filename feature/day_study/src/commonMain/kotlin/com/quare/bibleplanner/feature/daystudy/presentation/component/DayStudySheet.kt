package com.quare.bibleplanner.feature.daystudy.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel

private const val SHEET_HEIGHT_FRACTION = 0.92f
private const val SWAP_ANIMATION_MILLIS = 450
private const val SWAP_SLIDE_FRACTION = 24

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayStudySheet(
    study: DayStudyModel?,
    generation: DayStudyGenerationUiModel?,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(SHEET_HEIGHT_FRACTION),
        ) {
            AnimatedContent(
                targetState = generation != null,
                transitionSpec = { getSwapTransition() },
                label = "sheet_generation_swap",
            ) { isGenerating ->
                if (isGenerating) {
                    generation?.let { current ->
                        DayStudyGeneratingContent(
                            generation = current,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 24.dp,
                                ),
                        )
                    }
                } else {
                    study?.let { openStudy ->
                        LoadedSheetContent(
                            study = openStudy,
                            onDismiss = onDismiss,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadedSheetContent(
    study: DayStudyModel,
    onDismiss: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DayStudyHeader(
            passageLabel = study.passageLabel,
            onCloseClick = onDismiss,
            modifier = Modifier.padding(
                start = 20.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 12.dp,
            ),
        )
        DayStudyTabbedContent(
            study = study,
            contentMaxWidth = Dp.Unspecified,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 18.dp,
                end = 20.dp,
                bottom = 28.dp,
            ),
        )
    }
}

private fun getSwapTransition() = (
    fadeIn(animationSpec = tween(SWAP_ANIMATION_MILLIS)) + slideInVertically(
        animationSpec = tween(SWAP_ANIMATION_MILLIS),
        initialOffsetY = { it / SWAP_SLIDE_FRACTION },
    )
) togetherWith fadeOut(animationSpec = tween(SWAP_ANIMATION_MILLIS))
