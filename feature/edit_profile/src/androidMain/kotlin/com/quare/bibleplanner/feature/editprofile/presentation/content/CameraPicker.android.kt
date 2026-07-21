package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
actual fun rememberCameraPicker(onResult: (PlatformFile?) -> Unit): () -> Unit {
    val launcher = rememberCameraPickerLauncher(onResult = onResult)
    return { launcher.launch(cameraFacing = FileKitCameraFacing.Front) }
}
