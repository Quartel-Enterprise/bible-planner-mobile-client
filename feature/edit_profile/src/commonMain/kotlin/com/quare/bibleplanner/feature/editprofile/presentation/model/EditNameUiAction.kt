package com.quare.bibleplanner.feature.editprofile.presentation.model

internal sealed interface EditNameUiAction {
    data object NotifyNameUpdated : EditNameUiAction
}
