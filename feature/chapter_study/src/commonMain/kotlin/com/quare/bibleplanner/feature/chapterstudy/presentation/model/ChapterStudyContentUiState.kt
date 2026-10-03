package com.quare.bibleplanner.feature.chapterstudy.presentation.model

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel

internal sealed interface ChapterStudyContentUiState {
    data object Loading : ChapterStudyContentUiState

    /** The study beside the reader waits for the user to ask for it, so it never spends a study on its own. */
    data class NotGenerated(
        val hero: ChapterStudyHeroUiModel,
        val isStarting: Boolean,
    ) : ChapterStudyContentUiState

    data class Generating(
        val currentPhaseIndex: Int,
    ) : ChapterStudyContentUiState

    data class Failed(
        val isOffline: Boolean,
    ) : ChapterStudyContentUiState

    data class Loaded(
        val study: ChapterStudyModel,
        val keyVerseText: String?,
    ) : ChapterStudyContentUiState
}
