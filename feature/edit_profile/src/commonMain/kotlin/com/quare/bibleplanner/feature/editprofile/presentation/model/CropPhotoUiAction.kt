package com.quare.bibleplanner.feature.editprofile.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface CropPhotoUiAction {
    data object NavigateBack : CropPhotoUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : CropPhotoUiAction
}
