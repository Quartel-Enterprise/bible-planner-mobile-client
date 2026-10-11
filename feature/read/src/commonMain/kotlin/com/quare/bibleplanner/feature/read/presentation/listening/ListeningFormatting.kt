package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import kotlin.math.roundToInt
import kotlin.time.Duration

private const val SECONDS_PER_MINUTE = 60
private const val CLOCK_SECONDS_DIGITS = 2

internal fun Duration.toClockText(): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val seconds = (totalSeconds % SECONDS_PER_MINUTE).toString().padStart(CLOCK_SECONDS_DIGITS, '0')
    return "${totalSeconds / SECONDS_PER_MINUTE}:$seconds"
}

internal fun getListeningMinutes(
    verseTexts: List<String>,
    speed: Float,
): Int {
    val total = EstimateListeningTimeUseCase()(
        verseTexts = verseTexts,
        speed = speed,
    )
    return (total.inWholeSeconds.toFloat() / SECONDS_PER_MINUTE).roundToInt().coerceAtLeast(1)
}
