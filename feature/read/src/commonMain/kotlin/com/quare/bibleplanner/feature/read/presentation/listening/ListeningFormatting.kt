package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val CLOCK_SECONDS_DIGITS = 2

internal fun Duration.toClockText(): String = coerceAtLeast(Duration.ZERO).toComponents { minutes, seconds, _ ->
    "$minutes:${seconds.toString().padStart(CLOCK_SECONDS_DIGITS, '0')}"
}

internal fun getListeningMinutes(
    verseTexts: List<String>,
    speed: Float,
): Int {
    val total = EstimateListeningTimeUseCase()(
        verseTexts = verseTexts,
        speed = speed,
    )
    return total.toDouble(DurationUnit.MINUTES).roundToInt().coerceAtLeast(1)
}
