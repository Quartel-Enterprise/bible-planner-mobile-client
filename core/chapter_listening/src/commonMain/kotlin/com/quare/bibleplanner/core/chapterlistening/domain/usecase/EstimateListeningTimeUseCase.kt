package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningTimeModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/*
 * Why: before a chapter's audio is fetched there is no duration to read, so the time shown next to the
 * listen entry is estimated from the word count at the pace the voices read at 1x.
 */
class EstimateListeningTimeUseCase {
    private val whitespace = Regex("\\s+")

    operator fun invoke(
        wordCounts: List<Int>,
        verseIndex: Int,
        verseProgress: Float,
        speed: Float,
    ): ListeningTimeModel {
        val wordsBefore = wordCounts.take(verseIndex).sum()
        val currentWords = wordCounts.getOrElse(verseIndex) { 0 } * verseProgress.coerceIn(0f, 1f)
        return ListeningTimeModel(
            elapsed = toDuration(
                words = wordsBefore + currentWords,
                speed = speed,
            ),
            total = toDuration(
                words = wordCounts.sum().toFloat(),
                speed = speed,
            ),
        )
    }

    fun countWords(text: String): Int = text.split(whitespace).count(String::isNotBlank)

    private fun toDuration(
        words: Float,
        speed: Float,
    ): Duration = (words / (WORDS_PER_SECOND * speed)).toDouble().seconds

    private companion object {
        const val WORDS_PER_SECOND = 2.5f
    }
}
