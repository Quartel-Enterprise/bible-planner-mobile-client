package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface SpeechVoicesModel {
    data object Loading : SpeechVoicesModel

    data object Unavailable : SpeechVoicesModel

    data class Available(
        val voices: List<ListeningVoiceModel>,
        val shouldSuggestEnhancedVoice: Boolean,
    ) : SpeechVoicesModel
}
