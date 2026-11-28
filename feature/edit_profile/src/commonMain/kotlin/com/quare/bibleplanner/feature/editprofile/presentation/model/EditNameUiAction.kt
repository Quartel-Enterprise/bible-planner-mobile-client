package com.quare.bibleplanner.feature.editprofile.presentation.model

sealed interface EditNameUiAction {
    data object NotifyNameUpdated : EditNameUiAction
}
