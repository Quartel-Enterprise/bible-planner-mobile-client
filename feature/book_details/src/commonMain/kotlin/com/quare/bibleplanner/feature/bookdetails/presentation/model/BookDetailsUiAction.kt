package com.quare.bibleplanner.feature.bookdetails.presentation.model

sealed interface BookDetailsUiAction {
    data object NavigateBack : BookDetailsUiAction
}
