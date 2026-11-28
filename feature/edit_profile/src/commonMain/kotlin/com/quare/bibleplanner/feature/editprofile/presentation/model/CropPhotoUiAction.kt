package com.quare.bibleplanner.feature.editprofile.presentation.model

sealed interface CropPhotoUiAction {
    data class ShowSnackbar(
        val message: EditProfileMessage,
    ) : CropPhotoUiAction
}
