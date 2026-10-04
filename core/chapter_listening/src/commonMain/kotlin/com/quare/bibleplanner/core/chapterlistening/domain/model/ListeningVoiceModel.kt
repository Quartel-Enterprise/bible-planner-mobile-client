package com.quare.bibleplanner.core.chapterlistening.domain.model

data class ListeningVoiceModel(
    val id: String,
    val name: String,
    val languageTag: String,
    val isEnhanced: Boolean,
)
