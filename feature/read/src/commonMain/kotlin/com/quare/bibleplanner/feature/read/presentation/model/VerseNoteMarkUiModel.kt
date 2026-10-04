package com.quare.bibleplanner.feature.read.presentation.model

data class VerseNoteMarkUiModel(
    val noteId: String,
    val noteVerseNumbers: List<Int>,
    val position: VerseNoteMarkPosition,
)
