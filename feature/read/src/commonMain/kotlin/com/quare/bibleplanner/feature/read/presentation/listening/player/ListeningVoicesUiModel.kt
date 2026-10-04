package com.quare.bibleplanner.feature.read.presentation.listening.player

sealed interface ListeningVoicesUiModel {
    data object Loading : ListeningVoicesUiModel

    data class Unavailable(
        val languageTag: String,
    ) : ListeningVoicesUiModel

    data class Available(
        val options: List<ListeningVoiceOptionUiModel>,
        val shouldSuggestEnhancedVoice: Boolean,
    ) : ListeningVoicesUiModel
}
