package com.quare.bibleplanner.feature.congrats.presentation.model

sealed interface CongratsUiAction {
    data object Close : CongratsUiAction
}
