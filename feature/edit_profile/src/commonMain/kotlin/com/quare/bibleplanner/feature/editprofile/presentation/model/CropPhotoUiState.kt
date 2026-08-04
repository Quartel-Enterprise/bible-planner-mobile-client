package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.image.PhotoOrientation

internal data class CropPhotoUiState(
    val image: ImageResult,
    val zoom: Float,
    val zoomRange: ClosedFloatingPointRange<Float>,
    val offsetX: Float,
    val offsetY: Float,
    val orientation: PhotoOrientation,
)
