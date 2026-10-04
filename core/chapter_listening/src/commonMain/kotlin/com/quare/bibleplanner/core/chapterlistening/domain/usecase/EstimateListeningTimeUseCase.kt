package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningTimeModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/*
 * Why: the system voices report no duration, so time is estimated from the word count at the pace
 * they speak at 1x. Words are counted once per verse, since the state changes on every spoken word.
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

    fun getDuration(
        wordCount: Int,
        speed: Float,
    ): Duration = toDuration(
        words = wordCount.toFloat(),
        speed = speed,
    )

    fun countWords(text: String): Int = text.split(whitespace).count(String::isNotBlank)

    private fun toDuration(
        words: Float,
        speed: Float,
    ): Duration = (words / (WORDS_PER_SECOND * speed)).toDouble().seconds

    private companion object {
        const val WORDS_PER_SECOND = 2.5f
    }
}
