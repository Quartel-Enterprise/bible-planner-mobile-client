package com.quare.bibleplanner.feature.read.presentation.model

import org.jetbrains.compose.resources.StringResource

/**
 * Represents the UI state for the Read screen.
 */
sealed interface ReadUiState {
    val bookStringResource: StringResource
    val chapterNumber: Int
    val isChapterRead: Boolean

    /**
     * Initial loading state when fetching chapter data.
     */
    data class Loading(
        override val chapterNumber: Int,
        override val bookStringResource: StringResource,
        override val isChapterRead: Boolean,
    ) : ReadUiState

    /**
     * State when chapter content is successfully loaded.
     */
    data class Success(
        override val chapterNumber: Int,
        override val bookStringResource: StringResource,
        override val isChapterRead: Boolean,
        val verses: List<VerseUiModel>,
    ) : ReadUiState

    /**
     * State when an error occurs while fetching data.
     */
    sealed interface Error : ReadUiState {
        val errorUiEvent: ReadUiEvent

        data class Unknown(
            override val chapterNumber: Int,
            override val bookStringResource: StringResource,
            override val isChapterRead: Boolean,
            override val errorUiEvent: ReadUiEvent,
        ) : Error

        data class ChapterNotFound(
            override val chapterNumber: Int,
            override val bookStringResource: StringResource,
            override val isChapterRead: Boolean,
            override val errorUiEvent: ReadUiEvent,
            val selectedBibleVersionName: String,
        ) : Error
    }
}
