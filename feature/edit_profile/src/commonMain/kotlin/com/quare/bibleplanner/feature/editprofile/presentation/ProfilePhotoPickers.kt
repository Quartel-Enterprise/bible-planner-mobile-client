package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
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
    val onFilePicked: (PlatformFile?) -> Unit = { file ->
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
        onLaunchCameraPicker = launchCamera,
    )
}
