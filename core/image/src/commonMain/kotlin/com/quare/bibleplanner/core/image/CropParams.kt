package com.quare.bibleplanner.core.image

data class CropParams(
    val imageWidth: Int,
    val imageHeight: Int,
    val circleDiameter: Float,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
)
