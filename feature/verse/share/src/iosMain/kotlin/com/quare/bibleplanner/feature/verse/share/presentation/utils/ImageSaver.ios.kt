package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery
import kotlinx.coroutines.launch

@Composable
internal actual fun rememberImageSaver(
    onResult: (ImageSaveResult) -> Unit,
): (imageBytes: ByteArray, fileName: String) -> Unit {
    val coroutineScope = rememberCoroutineScope()
    return { imageBytes, fileName ->
        coroutineScope.launch {
            FileKit
                .saveImageToGallery(
                    bytes = imageBytes,
                    filename = fileName,
                ).onSuccess { onResult(ImageSaveResult.SAVED) }
                .onFailure { onResult(ImageSaveResult.FAILED) }
        }
    }
}
