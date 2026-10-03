package com.quare.bibleplanner.feature.read.presentation.model

sealed interface ChapterLoadResult {
    data object ChapterMissing : ChapterLoadResult

    data object TextMissing : ChapterLoadResult

    data class Loaded(
        val chapter: ReadChapterUiModel,
    ) : ChapterLoadResult
}
