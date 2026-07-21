package com.quare.bibleplanner.feature.editprofile.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

internal sealed interface ProfilePhotoUiAction {
    data class OpenCrop(
        val route: NavKey,
    ) : ProfilePhotoUiAction

    data object PhotoChanged : ProfilePhotoUiAction

    data object LaunchGalleryPicker : ProfilePhotoUiAction

    data object LaunchCameraPicker : ProfilePhotoUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : ProfilePhotoUiAction
}
