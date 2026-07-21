package com.quare.bibleplanner.core.image

internal fun calculateSampleSize(
    width: Int,
    height: Int,
    targetPx: Int,
): Int {
    if (width <= 0 || height <= 0 || targetPx <= 0) return 1
    var sampleSize = 1
    while (minOf(width, height) / (sampleSize * 2) >= targetPx) {
        sampleSize *= 2
    }
    return sampleSize
}
