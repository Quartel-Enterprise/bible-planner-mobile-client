package com.quare.bibleplanner.core.image

data class PhotoOrientation(
    val rotationDegrees: Int,
    val isFlippedHorizontally: Boolean,
    val isFlippedVertically: Boolean,
) {
    val isQuarterTurned: Boolean
        get() = rotationDegrees % HALF_TURN_DEGREES != 0

    val isOriginal: Boolean
        get() = rotationDegrees == NO_ROTATION && !isFlippedHorizontally && !isFlippedVertically

    val horizontalScale: Float
        get() = if (isFlippedHorizontally) MIRRORED_SCALE else NORMAL_SCALE

    val verticalScale: Float
        get() = if (isFlippedVertically) MIRRORED_SCALE else NORMAL_SCALE

    fun rotatedQuarterTurn(): PhotoOrientation =
        copy(rotationDegrees = (rotationDegrees + QUARTER_TURN_DEGREES) % FULL_TURN_DEGREES)

    fun flippedHorizontally(): PhotoOrientation = if (isQuarterTurned) {
        copy(isFlippedVertically = !isFlippedVertically)
    } else {
        copy(isFlippedHorizontally = !isFlippedHorizontally)
    }

    fun flippedVertically(): PhotoOrientation = if (isQuarterTurned) {
        copy(isFlippedHorizontally = !isFlippedHorizontally)
    } else {
        copy(isFlippedVertically = !isFlippedVertically)
    }

    companion object {
        const val NO_ROTATION = 0
        private const val QUARTER_TURN_DEGREES = 90
        private const val HALF_TURN_DEGREES = 180
        private const val FULL_TURN_DEGREES = 360
        private const val NORMAL_SCALE = 1f
        private const val MIRRORED_SCALE = -1f
    }
}
