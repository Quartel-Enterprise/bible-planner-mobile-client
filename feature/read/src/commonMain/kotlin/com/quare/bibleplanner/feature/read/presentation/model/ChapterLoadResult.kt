package com.quare.bibleplanner.feature.read.presentation.model

/** Why a chapter did or did not make it to the screen, which is what picks the error the reader shows. */
sealed interface ChapterLoadResult {
    /** No chapter row at all: the local bible index has not been built yet, so retrying makes sense. */
    data object ChapterMissing : ChapterLoadResult

    /** The chapter exists but the selected version has no text for it, so it has to be downloaded. */
    data object TextMissing : ChapterLoadResult

    data class Loaded(
        val chapter: ReadChapterUiModel,
    ) : ChapterLoadResult
}
