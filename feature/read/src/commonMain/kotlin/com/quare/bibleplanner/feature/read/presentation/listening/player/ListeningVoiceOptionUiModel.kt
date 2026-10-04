package com.quare.bibleplanner.feature.read.presentation.listening.player

data class ListeningVoiceOptionUiModel(
    val id: String,
    val name: String,
    val position: Int,
    val languageTag: String,
    val isEnhanced: Boolean,
    val isSelected: Boolean,
    val isPreviewing: Boolean,
)
