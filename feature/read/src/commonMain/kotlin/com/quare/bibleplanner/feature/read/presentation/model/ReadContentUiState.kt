package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel

sealed interface ReadContentUiState {
    data object Loading : ReadContentUiState

    data class Success(
        val chapters: List<ReadChapterUiModel>,
    ) : ReadContentUiState

    sealed interface Error : ReadContentUiState {
        val errorUiEvent: ReadUiEvent

        data class Unknown(
            override val errorUiEvent: ReadUiEvent,
        ) : Error

        data class ChapterNotFound(
            override val errorUiEvent: ReadUiEvent,
            val selectedBibleVersionName: String,
            val downloadStatus: DownloadStatusModel,
            val versionSizeInBytes: Long?,
        ) : Error
    }
}
