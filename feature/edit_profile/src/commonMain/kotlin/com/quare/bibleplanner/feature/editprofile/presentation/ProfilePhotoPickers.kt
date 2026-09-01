package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.feature.editprofile.presentation.content.CameraLaunchOverlay
import com.quare.bibleplanner.feature.editprofile.presentation.content.rememberCameraPicker
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.utils.ProfilePhotoUiActionCollector
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

@Composable
internal fun ProfilePhotoPickers(
    viewModel: ProfilePhotoViewModel,
    onOpenCrop: (NavKey) -> Unit,
    onPhotoChanged: () -> Unit,
) {
    var isLaunchingCamera by remember { mutableStateOf(false) }
    val onFilePicked: (PlatformFile?) -> Unit = { file ->
        isLaunchingCamera = false
        viewModel.onEvent(ProfilePhotoUiEvent.OnImagePicked(file))
    }
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = onFilePicked,
    )
    val launchCamera = rememberCameraPicker(onResult = onFilePicked)

    ProfilePhotoUiActionCollector(
        uiActionFlow = viewModel.uiAction,
        onOpenCrop = onOpenCrop,
        onPhotoChanged = onPhotoChanged,
        onLaunchGalleryPicker = galleryLauncher::launch,
        onLaunchCameraPicker = {
            isLaunchingCamera = true
            launchCamera()
        },
    )
    if (isLaunchingCamera) {
        CameraLaunchOverlay()
    }
}
