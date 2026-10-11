package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/*
 * Why: before a chapter's audio is fetched there is no duration to read, so the time shown next to the
 * listen entry is estimated from the word count at the pace the voices read at 1x.
 */
class EstimateListeningTimeUseCase {
    private val whitespace = Regex("\\s+")

    operator fun invoke(
        verseTexts: List<String>,
        speed: Float,
    ): Duration {
        val words = verseTexts.sumOf { text -> text.split(whitespace).count(String::isNotBlank) }
        return (words / (WORDS_PER_SECOND * speed)).toDouble().seconds
    }

    private companion object {
        const val WORDS_PER_SECOND = 2.5f
    }
}
