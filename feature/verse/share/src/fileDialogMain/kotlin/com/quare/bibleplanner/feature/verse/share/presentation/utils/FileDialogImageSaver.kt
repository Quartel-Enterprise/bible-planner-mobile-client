package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch

private const val FILE_EXTENSION_SEPARATOR = "."

@Composable
internal fun rememberFileDialogImageSaver(
    onResult: (ImageSaveResult) -> Unit,
): (imageBytes: ByteArray, fileName: String) -> Unit {
    val coroutineScope = rememberCoroutineScope()
    /*
     * Why: the dialog result can reach a recreated activity (rotation, or process death on Android 9
     * and older), so the image waits in a cache file whose name survives in the saved state.
     */
    var pendingFileName by rememberSaveable { mutableStateOf<String?>(null) }
    val fileSaver = rememberFileSaverLauncher(dialogSettings = FileKitDialogSettings.createDefault()) { destination ->
        val pendingFile = pendingFileName?.let { FileKit.cacheDir / it }
        pendingFileName = null
        coroutineScope.launch {
            val result = if (destination == null || pendingFile == null) {
                ImageSaveResult.CANCELLED
            } else {
                suspendRunCatching { destination.write(pendingFile.readBytes()) }.fold(
                    onSuccess = { ImageSaveResult.SAVED },
                    onFailure = { ImageSaveResult.FAILED },
                )
            }
            pendingFile?.let { suspendRunCatching { it.delete() } }
            onResult(result)
        }
    }
    return { imageBytes, fileName ->
        coroutineScope.launch {
            suspendRunCatching { (FileKit.cacheDir / fileName).write(imageBytes) }
                .onSuccess {
                    pendingFileName = fileName
                    fileSaver.launch(
                        suggestedName = fileName.substringBeforeLast(FILE_EXTENSION_SEPARATOR),
                        defaultExtension = fileName.substringAfterLast(FILE_EXTENSION_SEPARATOR),
                    )
                }.onFailure { onResult(ImageSaveResult.FAILED) }
        }
    }
}
