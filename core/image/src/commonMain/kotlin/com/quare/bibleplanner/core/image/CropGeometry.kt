package com.quare.bibleplanner.core.image

fun circleCoverScale(
    imageWidth: Int,
    imageHeight: Int,
    circleDiameter: Float,
): Float = circleDiameter / minOf(imageWidth, imageHeight)

internal fun computeCropRect(params: CropParams): NormalizedCropRect = with(params) {
    val effectiveScale = circleCoverScale(
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        circleDiameter = circleDiameter,
    ) * zoom
    val sideInImage = circleDiameter / effectiveScale

    val left = (imageWidth / 2f - (circleDiameter / 2f + offsetX) / effectiveScale)
        .coerceIn(0f, (imageWidth - sideInImage).coerceAtLeast(0f))
    val top = (imageHeight / 2f - (circleDiameter / 2f + offsetY) / effectiveScale)
        .coerceIn(0f, (imageHeight - sideInImage).coerceAtLeast(0f))

    NormalizedCropRect(
        left = left / imageWidth,
        top = top / imageHeight,
        size = sideInImage / minOf(imageWidth, imageHeight),
    )
}

fun maxPanOffset(
    imageWidth: Int,
    imageHeight: Int,
    circleDiameter: Float,
    zoom: Float,
): Pair<Float, Float> {
    val effectiveScale = circleCoverScale(
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        circleDiameter = circleDiameter,
    ) * zoom
    val maxX = (imageWidth * effectiveScale - circleDiameter).coerceAtLeast(0f) / 2f
    val maxY = (imageHeight * effectiveScale - circleDiameter).coerceAtLeast(0f) / 2f
    return maxX to maxY
}
