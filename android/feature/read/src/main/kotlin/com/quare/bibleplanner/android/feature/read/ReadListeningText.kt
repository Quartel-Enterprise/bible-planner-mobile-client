package com.quare.bibleplanner.android.feature.read

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val CLOCK_SECONDS_DIGITS = 2

@Composable
internal fun chapterTitle(chapter: ChapterLocationModel): String = verseReference(
    chapter = chapter,
    verseNumber = null,
)

@Composable
internal fun verseReference(
    chapter: ChapterLocationModel,
    verseNumber: Int?,
): String = verseReferenceLabel(
    bookId = chapter.bookId,
    chapterNumber = chapter.chapterNumber,
    verseNumbers = listOfNotNull(verseNumber),
)

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

@Composable
internal fun listeningWordStyle(): SpanStyle = SpanStyle(
    color = MaterialTheme.colorScheme.onPrimary,
    background = MaterialTheme.colorScheme.primary,
)

internal fun String.withListeningWord(
    word: IntRange?,
    style: SpanStyle,
): AnnotatedString = AnnotatedString(this).withListeningWord(
    word = word,
    style = style,
)

internal fun AnnotatedString.withListeningWord(
    word: IntRange?,
    style: SpanStyle,
): AnnotatedString {
    // Why: the range comes from another copy of the text, so one that no longer fits it is dropped.
    val safeWord = word?.takeIf { it.first >= 0 && it.last < length } ?: return this
    return AnnotatedString
        .Builder(this)
        .apply {
            addStyle(
                style = style,
                start = safeWord.first,
                end = safeWord.last + 1,
            )
        }.toAnnotatedString()
}

// Why: the player's range counts from the trimmed verse text, and the reader shows it untrimmed.
internal fun IntRange.toUntrimmed(text: String): IntRange {
    val leadingSpaces = text.length - text.trimStart().length
    return first + leadingSpaces..last + leadingSpaces
}
