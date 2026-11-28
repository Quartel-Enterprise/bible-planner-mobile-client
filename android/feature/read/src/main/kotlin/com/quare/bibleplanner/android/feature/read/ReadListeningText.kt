package com.quare.bibleplanner.android.feature.read

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

@Composable
internal fun verseReference(
    chapter: ChapterLocationModel,
    verseNumber: Int?,
): String = verseReferenceLabel(
    bookId = chapter.bookId,
    chapterNumber = chapter.chapterNumber,
    verseNumbers = listOfNotNull(verseNumber),
)

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
