package com.quare.bibleplanner.core.image

data class CropParams(
    val imageWidth: Int,
    val imageHeight: Int,
    val circleDiameter: Float,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
    val orientation: PhotoOrientation,
) {
    val orientedWidth: Int
        get() = if (orientation.isQuarterTurned) imageHeight else imageWidth

    val orientedHeight: Int
        get() = if (orientation.isQuarterTurned) imageWidth else imageHeight
}
