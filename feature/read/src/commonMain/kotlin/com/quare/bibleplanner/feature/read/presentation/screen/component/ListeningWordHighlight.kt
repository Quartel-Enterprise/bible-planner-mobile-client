package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

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
