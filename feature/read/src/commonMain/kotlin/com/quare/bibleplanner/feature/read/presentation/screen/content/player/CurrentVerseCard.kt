package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.screen.component.listeningWordStyle
import com.quare.bibleplanner.feature.read.presentation.screen.component.verseReference
import com.quare.bibleplanner.feature.read.presentation.screen.component.withListeningWord
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily

private const val VERSE_MAX_LINES = 3

@Composable
internal fun CurrentVerseCard(
    player: ListeningPlayerUiModel,
    isWordHighlightEnabled: Boolean,
) {
    PlayerSection(verticalPadding = 14.dp) {
        Text(
            text = verseReference(
                chapter = player.chapter,
                verseNumber = player.verseNumber,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        player.verseText?.let { text ->
            // Why: without word timings nothing would scroll the card, so the verse keeps its ellipsis.
            if (isWordHighlightEnabled && player.hasWordTimings) {
                FollowedVerseText(
                    text = text,
                    highlightedWord = (player.spokenWord as? ListeningWordTargetModel.Verse)?.range,
                )
            } else {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = displaySerifFontFamily(),
                    maxLines = VERSE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/*
 * Why: a long verse doesn't fit the card, so instead of cutting it with an ellipsis the text scrolls
 * line by line to keep the word being read in view, with the line before it as context.
 */
@Composable
private fun FollowedVerseText(
    text: String,
    highlightedWord: IntRange?,
) {
    val style = MaterialTheme.typography.bodyLarge
    val scrollState = rememberScrollState()
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    // Why: right after the verse changes the layout is still the previous verse's, whose lines don't match.
    val currentLayout = layout?.takeIf { it.layoutInput.text.text == text }
    // Why: the pause after the verse's last word has no word, and the card must not jump back to its top then.
    val lastWordLine = remember(text) { LastWordLine() }
    val wordLine = highlightedWord
        ?.takeIf { word -> word.first < text.length }
        ?.let { word -> currentLayout?.getLineForOffset(word.first) }
        ?: lastWordLine.line
    lastWordLine.line = wordLine
    LaunchedEffect(text, wordLine) {
        val top = currentLayout?.getLineTop((wordLine - 1).coerceAtLeast(0)) ?: 0f
        scrollState.animateScrollTo(top.toInt())
    }
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .heightIn(max = with(LocalDensity.current) { (style.lineHeight * VERSE_MAX_LINES).toDp() })
            .verticalScroll(
                state = scrollState,
                enabled = false,
            ),
    ) {
        Text(
            text = text.withListeningWord(
                word = highlightedWord,
                style = listeningWordStyle(),
            ),
            style = style,
            fontFamily = displaySerifFontFamily(),
            onTextLayout = { layout = it },
        )
    }
}

// Why: a plain holder, since the remembered line only feeds the next composition and must not trigger one.
private class LastWordLine {
    var line: Int = 0
}
