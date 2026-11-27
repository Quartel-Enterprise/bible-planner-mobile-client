package com.quare.bibleplanner.feature.editprofile.presentation.model

internal sealed interface EditNameUiAction {
    data class ShowSnackbar(
        val message: EditProfileMessage,
    ) : EditNameUiAction
}
