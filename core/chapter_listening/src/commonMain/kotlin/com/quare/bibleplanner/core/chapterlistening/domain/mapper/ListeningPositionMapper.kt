package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import kotlin.time.Duration

internal fun ListeningSessionModel.withPosition(
    position: Duration,
    isWordHighlightEnabled: Boolean,
): ListeningSessionModel {
    val index = verses.indexOfLast { verse -> verse.start <= position }.coerceAtLeast(0)
    val verse = verses.getOrNull(index) ?: return this
    val length = verse.end - verse.start
    val progress = if (length > Duration.ZERO) ((position - verse.start) / length).toFloat() else 0f
    return copy(
        verseIndex = index,
        verseProgress = progress.coerceIn(0f, 1f),
        wordIndex = verse.findWordIndex(
            position = position,
            isWordHighlightEnabled = isWordHighlightEnabled,
        ),
    )
}

internal fun ListeningSessionModel.withCurrentWord(
    position: Duration,
    isWordHighlightEnabled: Boolean,
): ListeningSessionModel = copy(
    wordIndex = verses.getOrNull(verseIndex)?.findWordIndex(
        position = position,
        isWordHighlightEnabled = isWordHighlightEnabled,
    ),
)

/*
 * Why: the highlighted word moves when the audio crosses a word's start or end, not on the next
 * regular tick; the next verse's first word counts too, or it would light up late.
 */
internal fun ListeningSessionModel.findNextWordChange(position: Duration): Duration? {
    val words = verses
        .getOrNull(verseIndex)
        ?.words
        .orEmpty()
    val nextVerseFirstWord = verses
        .getOrNull(verseIndex + 1)
        ?.words
        ?.firstOrNull()
    return words
        .asSequence()
        .flatMap { word -> sequenceOf(word.start, word.end) }
        .plus(listOfNotNull(nextVerseFirstWord?.start))
        .firstOrNull { boundary -> boundary > position }
}

private fun ListeningVerseModel.findWordIndex(
    position: Duration,
    isWordHighlightEnabled: Boolean,
): Int? {
    if (!isWordHighlightEnabled) return null
    val index = words.indexOfLast { word -> word.start <= position }
    return index.takeIf { it >= 0 && position < words[it].end }
}
