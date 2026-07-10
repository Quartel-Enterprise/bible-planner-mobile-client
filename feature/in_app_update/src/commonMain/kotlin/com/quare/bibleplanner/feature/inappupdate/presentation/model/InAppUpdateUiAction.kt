package com.quare.bibleplanner.feature.inappupdate.presentation.model

internal sealed interface InAppUpdateUiAction {
    data object NavigateBack : InAppUpdateUiAction
}
