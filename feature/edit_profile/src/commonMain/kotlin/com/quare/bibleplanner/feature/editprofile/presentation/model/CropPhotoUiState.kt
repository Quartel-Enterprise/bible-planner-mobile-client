package com.quare.bibleplanner.feature.editprofile.presentation.model

internal data class CropPhotoUiState(
    val image: ImageResult,
    val zoom: Float,
    val zoomRange: ClosedFloatingPointRange<Float>,
    val offsetX: Float,
    val offsetY: Float,
)
