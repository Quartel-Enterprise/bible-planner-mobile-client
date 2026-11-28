package com.quare.bibleplanner.android.feature.listeningplayer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily

private const val VERSE_MAX_LINES = 3
private const val DISABLED_ICON_ALPHA = 0.38f
private val playButtonSize = 68.dp
private val verseButtonSize = 52.dp
private val chapterButtonWidth = 64.dp

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
    val layoutHolder = remember { TextLayoutHolder() }
    // Why: only a change in the number of lines moves the word to another line, so only that recomposes.
    var lineCount by remember { mutableIntStateOf(0) }
    // Why: right after the verse changes the layout is still the previous verse's, whose lines don't match.
    val currentLayout = layoutHolder.result?.takeIf { it.layoutInput.text.text == text }
    // Why: the pause after the verse's last word has no word, and the card must not jump back to its top then.
    val lastWordLine = remember(text) { LastWordLine() }
    val wordLine = highlightedWord
        ?.takeIf { word -> word.first in text.indices }
        ?.let { word -> currentLayout?.getLineForOffset(word.first) }
        ?: lastWordLine.line
    lastWordLine.line = wordLine
    LaunchedEffect(text, wordLine, lineCount) {
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
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onPrimary,
                    background = MaterialTheme.colorScheme.primary,
                ),
            ),
            style = style,
            fontFamily = displaySerifFontFamily(),
            onTextLayout = { result ->
                layoutHolder.result = result
                lineCount = result.lineCount
            },
        )
    }
}

// Why: a plain holder, since the remembered line only feeds the next composition and must not trigger one.
private class LastWordLine {
    var line: Int = 0
}

// Why: a plain holder, since a new layout on every spoken word must not recompose the card again.
private class TextLayoutHolder {
    var result: TextLayoutResult? = null
}

private fun String.withListeningWord(
    word: IntRange?,
    style: SpanStyle,
): AnnotatedString {
    // Why: the range comes from another copy of the text, so one that no longer fits it is dropped.
    val safeWord = word?.takeIf { it.first >= 0 && it.last < length } ?: return AnnotatedString(this)
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

@Composable
internal fun ProgressSection(
    player: ListeningPlayerUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    var draggedValue by remember { mutableStateOf<Float?>(null) }
    val animatedValue by animateFloatAsState(targetValue = player.progress)
    val maxIndex = (player.verseCount - 1).coerceAtLeast(0)
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = draggedValue ?: animatedValue,
            enabled = player.verseCount > 0,
            onValueChange = { value -> draggedValue = value },
            onValueChangeFinished = {
                draggedValue?.let { value ->
                    onEvent(ChapterListeningPlayerUiEvent.OnSeek((value * maxIndex).toInt().coerceIn(0, maxIndex)))
                }
                draggedValue = null
            },
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(
                    R.string.listening_player_verse_of,
                    player.verseNumber ?: 0,
                    player.verseCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.listening_player_time_remaining,
                    player.elapsed.toClockText(),
                    player.remaining.toClockText(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ControlsRow(
    uiState: ChapterListeningPlayerUiState,
    player: ListeningPlayerUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val isPlaying = player.status == ListeningStatusModel.PLAYING
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 6.dp,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        ChapterJumpButton(
            imageVector = Icons.Rounded.KeyboardDoubleArrowLeft,
            label = uiState.previousChapter?.let { chapterTitle(it) },
            onClick = { onEvent(ChapterListeningPlayerUiEvent.OnPreviousChapterClick) },
        )
        IconButton(
            modifier = Modifier.size(verseButtonSize),
            onClick = { onEvent(ChapterListeningPlayerUiEvent.OnPreviousVerseClick) },
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = stringResource(R.string.listening_player_previous_verse),
            )
        }
        FilledIconButton(
            modifier = Modifier.size(playButtonSize),
            onClick = { onEvent(ChapterListeningPlayerUiEvent.OnPlayPauseClick) },
            enabled = player.status != ListeningStatusModel.NEXT_LOCKED,
        ) {
            if (player.status == ListeningStatusModel.PREPARING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    modifier = Modifier.size(38.dp),
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.listening_player_pause else R.string.listening_player_play,
                    ),
                )
            }
        }
        IconButton(
            modifier = Modifier.size(verseButtonSize),
            onClick = { onEvent(ChapterListeningPlayerUiEvent.OnNextVerseClick) },
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = stringResource(R.string.listening_player_next_verse),
            )
        }
        ChapterJumpButton(
            imageVector = Icons.Rounded.KeyboardDoubleArrowRight,
            label = uiState.nextChapter?.let { chapterTitle(it) },
            onClick = { onEvent(ChapterListeningPlayerUiEvent.OnNextChapterClick) },
        )
    }
}

@Composable
private fun ChapterJumpButton(
    imageVector: ImageVector,
    label: String?,
    onClick: () -> Unit,
) {
    val iconAlpha = if (label == null) DISABLED_ICON_ALPHA else 1f
    Column(
        modifier = Modifier
            .width(chapterButtonWidth)
            .clip(RoundedCornerShape(12.dp))
            // Why: a disabled clickable with no label is announced as an unnamed control, so the first chapter's has none.
            .then(
                if (label == null) {
                    Modifier
                } else {
                    Modifier.clickable(
                        role = Role.Button,
                        onClick = onClick,
                    )
                },
            ).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(26.dp),
            imageVector = imageVector,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = iconAlpha),
        )
        Text(
            text = label.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
