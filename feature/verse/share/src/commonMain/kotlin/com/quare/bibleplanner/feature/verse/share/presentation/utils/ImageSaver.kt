package com.quare.bibleplanner.feature.verse.share.presentation.utils

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult

@Composable
internal expect fun rememberImageSaver(
    onResult: (ImageSaveResult) -> Unit,
): (imageBytes: ByteArray, fileName: String) -> Unit
