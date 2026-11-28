package com.quare.bibleplanner.feature.editprofile.presentation.model

import androidx.compose.ui.graphics.ImageBitmap

sealed interface ImageResult {
    data object Loading : ImageResult

    data class Loaded(
        val bitmap: ImageBitmap,
    ) : ImageResult

    data object Failed : ImageResult
}
