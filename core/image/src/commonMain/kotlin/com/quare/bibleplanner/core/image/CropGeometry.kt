package com.quare.bibleplanner.core.image

fun getCircleCoverScale(
    imageWidth: Int,
    imageHeight: Int,
    circleDiameter: Float,
): Float = circleDiameter / minOf(imageWidth, imageHeight)

internal fun computeCropRect(params: CropParams): NormalizedCropRect = with(params) {
    val effectiveScale = getCircleCoverScale(
        imageWidth = orientedWidth,
        imageHeight = orientedHeight,
        circleDiameter = circleDiameter,
    ) * zoom
    val sideInImage = circleDiameter / effectiveScale

    val left = (orientedWidth / 2f - (circleDiameter / 2f + offsetX) / effectiveScale)
        .coerceIn(0f, (orientedWidth - sideInImage).coerceAtLeast(0f))
    val top = (orientedHeight / 2f - (circleDiameter / 2f + offsetY) / effectiveScale)
        .coerceIn(0f, (orientedHeight - sideInImage).coerceAtLeast(0f))

    NormalizedCropRect(
        left = left / orientedWidth,
        top = top / orientedHeight,
        size = sideInImage / minOf(orientedWidth, orientedHeight),
    )
}

fun getMaxPanOffset(
    imageWidth: Int,
    imageHeight: Int,
    circleDiameter: Float,
    zoom: Float,
    orientation: PhotoOrientation,
): Pair<Float, Float> {
    val orientedWidth = if (orientation.isQuarterTurned) imageHeight else imageWidth
    val orientedHeight = if (orientation.isQuarterTurned) imageWidth else imageHeight
    val effectiveScale = getCircleCoverScale(
        imageWidth = orientedWidth,
        imageHeight = orientedHeight,
        circleDiameter = circleDiameter,
    ) * zoom
    val maxX = (orientedWidth * effectiveScale - circleDiameter).coerceAtLeast(0f) / 2f
    val maxY = (orientedHeight * effectiveScale - circleDiameter).coerceAtLeast(0f) / 2f
    return maxX to maxY
}
