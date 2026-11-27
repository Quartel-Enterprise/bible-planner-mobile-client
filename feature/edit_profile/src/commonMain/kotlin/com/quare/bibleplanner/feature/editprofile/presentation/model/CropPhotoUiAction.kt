package com.quare.bibleplanner.feature.editprofile.presentation.model

internal sealed interface CropPhotoUiAction {
    data class ShowSnackbar(
        val message: EditProfileMessage,
    ) : CropPhotoUiAction
}
