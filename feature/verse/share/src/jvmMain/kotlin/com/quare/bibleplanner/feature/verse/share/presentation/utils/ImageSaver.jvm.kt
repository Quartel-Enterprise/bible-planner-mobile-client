package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch

private const val FILE_EXTENSION_SEPARATOR = "."

/*
 * Why: the desktop window is never recreated while the dialog is open, so the image waits in memory;
 * FileKit's cache directory on the JVM needs FileKit.init(appId), which the desktop app never calls.
 */
@Composable
internal actual fun rememberImageSaver(
    onResult: (ImageSaveResult) -> Unit,
): (imageBytes: ByteArray, fileName: String) -> Unit {
    val coroutineScope = rememberCoroutineScope()
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
    val fileSaver = rememberFileSaverLauncher(dialogSettings = FileKitDialogSettings.createDefault()) { destination ->
        val bytes = pendingBytes
        pendingBytes = null
        coroutineScope.launch {
            val result = if (destination == null || bytes == null) {
                ImageSaveResult.CANCELLED
            } else {
                suspendRunCatching { destination.write(bytes) }.fold(
                    onSuccess = { ImageSaveResult.SAVED },
                    onFailure = { ImageSaveResult.FAILED },
                )
            }
            onResult(result)
        }
    }
    return { imageBytes, fileName ->
        pendingBytes = imageBytes
        fileSaver.launch(
            suggestedName = fileName.substringBeforeLast(FILE_EXTENSION_SEPARATOR),
            defaultExtension = fileName.substringAfterLast(FILE_EXTENSION_SEPARATOR),
        )
    }
}
