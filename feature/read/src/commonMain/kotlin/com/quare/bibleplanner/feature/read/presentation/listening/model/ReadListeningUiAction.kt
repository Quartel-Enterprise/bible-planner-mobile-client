package com.quare.bibleplanner.feature.read.presentation.listening.model

sealed interface ReadListeningUiAction {
    data object ShowSleepTimerEnded : ReadListeningUiAction
}
