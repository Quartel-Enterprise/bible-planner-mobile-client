package com.quare.bibleplanner.feature.verse.share.presentation.utils

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery
import kotlinx.coroutines.launch

/*
 * Why: before Android 10 writing to the gallery needs the storage permission, so older devices save
 * through the system file picker instead, which needs none.
 */
@Composable
internal actual fun rememberImageSaver(
    onResult: (ImageSaveResult) -> Unit,
): (imageBytes: ByteArray, fileName: String) -> Unit {
    val coroutineScope = rememberCoroutineScope()
    val saveThroughDialog = rememberFileDialogImageSaver(onResult)
    return { imageBytes, fileName ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            coroutineScope.launch {
                FileKit
                    .saveImageToGallery(
                        bytes = imageBytes,
                        filename = fileName,
                    ).onSuccess { onResult(ImageSaveResult.SAVED) }
                    .onFailure { onResult(ImageSaveResult.FAILED) }
            }
        } else {
            saveThroughDialog(imageBytes, fileName)
        }
    }
}
