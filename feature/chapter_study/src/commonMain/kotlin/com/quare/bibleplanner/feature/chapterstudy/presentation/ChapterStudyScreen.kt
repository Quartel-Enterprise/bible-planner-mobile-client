package com.quare.bibleplanner.feature.chapterstudy.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.AskAiFab
import com.quare.bibleplanner.feature.chapterstudy.presentation.component.ChapterStudyTopBar
import com.quare.bibleplanner.feature.chapterstudy.presentation.content.ChapterStudyContent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiState
import com.quare.bibleplanner.ui.utils.asStable

@Composable
internal fun ChapterStudyScreen(
    uiState: ChapterStudyUiState,
    isBesideReader: Boolean,
    onEvent: (ChapterStudyUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            // Beside the reader the chapter is already in sight, and back leaves both panes.
            if (!isBesideReader) {
                ChapterStudyTopBar(
                    uiState = uiState,
                    onNavigateBack = onNavigateBack,
                )
            }
        },
        floatingActionButton = {
            if (uiState.content is ChapterStudyContentUiState.Loaded) {
                AskAiFab(onClick = { onEvent(ChapterStudyUiEvent.OnAskAiClick) })
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.asStable(),
    ) { paddingValues ->
        ChapterStudyContent(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier.padding(paddingValues),
        )
    }
}
