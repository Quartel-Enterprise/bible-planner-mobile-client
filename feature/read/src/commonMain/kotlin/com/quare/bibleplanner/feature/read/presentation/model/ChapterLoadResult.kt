package com.quare.bibleplanner.feature.read.presentation.model

sealed interface ChapterLoadResult {
    // Why: no chapter row means the local bible index isn't built yet, so retrying makes sense.
    data object ChapterMissing : ChapterLoadResult

    data object TextMissing : ChapterLoadResult

    data class Loaded(
        val chapter: ReadChapterUiModel,
    ) : ChapterLoadResult
}
