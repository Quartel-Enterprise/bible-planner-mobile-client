package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.feature.editprofile.presentation.content.rememberCameraPicker
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.koin.compose.koinInject

private val scrimColor = Color.Black.copy(alpha = 0.35f)

@Composable
internal fun ProfilePhotoPickersComponent(
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
    // Why: the photo snackbars come right after the sheet pops, so they go to the root host and outlive it.
    val appSnackbarController = koinInject<AppSnackbarController>()
    val context = LocalContext.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is ProfilePhotoUiAction.OpenCrop -> onOpenCrop(action.route)

            ProfilePhotoUiAction.PhotoChanged -> onPhotoChanged()

            ProfilePhotoUiAction.LaunchGalleryPicker -> galleryLauncher.launch()

            // Why: the overlay draws behind the sheet's window, so it can't stop a double tap from opening two cameras.
            ProfilePhotoUiAction.LaunchCameraPicker -> if (!isLaunchingCamera) {
                isLaunchingCamera = true
                launchCamera()
            }

            is ProfilePhotoUiAction.ShowSnackbar -> appSnackbarController.show(
                AppSnackbarMessage.Text(
                    text = context.getString(action.message.toStringRes()),
                    isDismissible = true,
                ),
            )
        }
    }
    if (isLaunchingCamera) {
        CameraLaunchOverlay()
    }
}

@Composable
private fun CameraLaunchOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scrimColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}
