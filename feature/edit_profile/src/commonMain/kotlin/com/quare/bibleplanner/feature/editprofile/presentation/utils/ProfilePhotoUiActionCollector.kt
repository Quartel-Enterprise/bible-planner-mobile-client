package com.quare.bibleplanner.feature.editprofile.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.LocalSnackbarHostState
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun ProfilePhotoUiActionCollector(
    uiActionFlow: Flow<ProfilePhotoUiAction>,
    onOpenCrop: (NavKey) -> Unit,
    onPhotoChanged: () -> Unit,
    onLaunchGalleryPicker: () -> Unit,
    onLaunchCameraPicker: () -> Unit,
) {
    val snackbarHostState = LocalSnackbarHostState.current
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            is ProfilePhotoUiAction.OpenCrop -> onOpenCrop(action.route)

            ProfilePhotoUiAction.PhotoChanged -> onPhotoChanged()

            ProfilePhotoUiAction.LaunchGalleryPicker -> onLaunchGalleryPicker()

            ProfilePhotoUiAction.LaunchCameraPicker -> onLaunchCameraPicker()

            is ProfilePhotoUiAction.ShowSnackbar -> snackbarHostState.showSnackbar(
                getString(action.message),
                withDismissAction = true,
            )
        }
    }
}
