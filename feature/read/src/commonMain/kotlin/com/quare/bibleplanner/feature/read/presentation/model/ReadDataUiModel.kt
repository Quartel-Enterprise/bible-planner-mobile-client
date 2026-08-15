package com.quare.bibleplanner.feature.read.presentation.model

/** What the reader knows about the chapter itself, before the user's own interaction is layered on. */
data class ReadDataUiModel(
    val header: ReadHeaderUiModel,
    val content: ReadContentUiState,
)
