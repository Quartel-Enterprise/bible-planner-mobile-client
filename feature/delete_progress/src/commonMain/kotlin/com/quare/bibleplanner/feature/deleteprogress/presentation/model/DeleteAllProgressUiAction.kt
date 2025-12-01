package com.quare.bibleplanner.feature.deleteprogress.presentation.model

sealed interface DeleteAllProgressUiAction {
    data object NavigateBack : DeleteAllProgressUiAction
}
