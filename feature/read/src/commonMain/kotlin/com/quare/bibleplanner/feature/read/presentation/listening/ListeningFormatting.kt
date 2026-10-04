package com.quare.bibleplanner.feature.read.presentation.listening

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_language_english
import bibleplanner.feature.read.generated.resources.listening_language_portuguese
import bibleplanner.feature.read.generated.resources.listening_language_spanish
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import org.jetbrains.compose.resources.StringResource
import kotlin.math.roundToInt
import kotlin.time.Duration

private const val SECONDS_PER_MINUTE = 60
private const val CLOCK_SECONDS_DIGITS = 2
private const val PORTUGUESE_CODE = "pt"
private const val SPANISH_CODE = "es"

internal fun Duration.toClockText(): String {
    val totalSeconds = inWholeSeconds.coerceAtLeast(0)
    val seconds = (totalSeconds % SECONDS_PER_MINUTE).toString().padStart(CLOCK_SECONDS_DIGITS, '0')
    return "${totalSeconds / SECONDS_PER_MINUTE}:$seconds"
}

internal fun getListeningMinutes(
    verseTexts: List<String>,
    speed: Float,
): Int {
    val estimateListeningTime = EstimateListeningTimeUseCase()
    val total = estimateListeningTime(
        wordCounts = verseTexts.map(estimateListeningTime::countWords),
        verseIndex = 0,
        verseProgress = 0f,
        speed = speed,
    ).total
    return (total.inWholeSeconds.toFloat() / SECONDS_PER_MINUTE).roundToInt().coerceAtLeast(1)
}

internal fun String.toLanguageNameResource(): StringResource = when (substringBefore('-').lowercase()) {
    PORTUGUESE_CODE -> Res.string.listening_language_portuguese
    SPANISH_CODE -> Res.string.listening_language_spanish
    else -> Res.string.listening_language_english
}
