package com.quare.bibleplanner.feature.bibleversion.presentation.model

sealed interface BibleVersionUiEvent {
    data class OnDownload(
        val id: String,
    ) : BibleVersionUiEvent

    data class OnPause(
        val id: String,
    ) : BibleVersionUiEvent

    data class OnResume(
        val id: String,
    ) : BibleVersionUiEvent

    data class OnDelete(
        val id: String,
    ) : BibleVersionUiEvent

    data class OnSelect(
        val id: String,
    ) : BibleVersionUiEvent

    data object OnDismiss : BibleVersionUiEvent
}
