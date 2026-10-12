package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_ai_voice
import bibleplanner.feature.read.generated.resources.listening_auto_next
import bibleplanner.feature.read.generated.resources.listening_auto_next_day
import bibleplanner.feature.read.generated.resources.listening_auto_next_off
import bibleplanner.feature.read.generated.resources.listening_auto_next_on
import bibleplanner.feature.read.generated.resources.listening_day_progress
import bibleplanner.feature.read.generated.resources.listening_day_title
import bibleplanner.feature.read.generated.resources.listening_locked_sheet
import bibleplanner.feature.read.generated.resources.listening_next_verse
import bibleplanner.feature.read.generated.resources.listening_pause
import bibleplanner.feature.read.generated.resources.listening_play
import bibleplanner.feature.read.generated.resources.listening_previous_verse
import bibleplanner.feature.read.generated.resources.listening_sleep_timer
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_end
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_minutes
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_off
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_pauses_in
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_stops_at_end
import bibleplanner.feature.read.generated.resources.listening_speed
import bibleplanner.feature.read.generated.resources.listening_time_remaining
import bibleplanner.feature.read.generated.resources.listening_unlock
import bibleplanner.feature.read.generated.resources.listening_verse_of
import bibleplanner.feature.read.generated.resources.listening_word_highlight
import bibleplanner.feature.read.generated.resources.listening_word_highlight_off
import bibleplanner.feature.read.generated.resources.listening_word_highlight_on
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.feature.read.presentation.listening.mapper.toUnavailableCopy
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningDayProgressUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningUnavailableCopy
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.feature.read.presentation.listening.player.ListeningSleepTimerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.toClockText
import com.quare.bibleplanner.feature.read.presentation.screen.component.chapterTitle
import com.quare.bibleplanner.feature.read.presentation.screen.component.listeningWordStyle
import com.quare.bibleplanner.feature.read.presentation.screen.component.verseReference
import com.quare.bibleplanner.feature.read.presentation.screen.component.withListeningWord
import com.quare.bibleplanner.ui.component.AppSwitch
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import org.jetbrains.compose.resources.stringResource

private const val SOFT_PRIMARY_ALPHA = 0.12f
private const val VERSE_MAX_LINES = 3
private val speedOptions = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
private val sleepTimerOptions = ListeningSleepTimerOption.entries
private val cardShape = RoundedCornerShape(16.dp)
private val playButtonSize = 68.dp
private val verseButtonSize = 52.dp
private val chapterButtonWidth = 64.dp
private val daySegmentHeight = 6.dp

@Composable
internal fun ChapterListeningPlayerContent(
    uiState: ChapterListeningPlayerUiState,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val player = uiState.player ?: return
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        player.dayProgress?.let { DayProgressCard(dayProgress = it, progress = player.progress) }
        CurrentVerseCard(
            player = player,
            isWordHighlightEnabled = uiState.isWordHighlightEnabled,
        )
        ProgressSection(
            player = player,
            onEvent = onEvent,
        )
        ControlsRow(
            uiState = uiState,
            player = player,
            onEvent = onEvent,
        )
        if (player.status == ListeningStatusModel.NEXT_LOCKED) {
            LockedCard(
                player = player,
                onEvent = onEvent,
            )
        }
        player.status.toUnavailableCopy()?.let { copy ->
            UnavailableCard(
                player = player,
                copy = copy,
                onEvent = onEvent,
            )
        }
        SpeedCard(
            speed = uiState.speed,
            onEvent = onEvent,
        )
        SleepTimerCard(
            sleepTimer = uiState.sleepTimer,
            onEvent = onEvent,
        )
        AutoNextCard(
            uiState = uiState,
            onEvent = onEvent,
        )
        WordHighlightCard(
            isEnabled = uiState.isWordHighlightEnabled,
            onEvent = onEvent,
        )
        AiVoiceLabel()
    }
}

@Composable
private fun DayProgressCard(
    dayProgress: ListeningDayProgressUiModel,
    progress: Float,
) {
    PlayerSection(verticalPadding = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.listening_day_title),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(
                    Res.string.listening_day_progress,
                    dayProgress.currentIndex + 1,
                    dayProgress.chapters.size,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            dayProgress.chapters.indices.forEach { index ->
                val fill = when {
                    index < dayProgress.currentIndex -> 1f
                    index == dayProgress.currentIndex -> progress
                    else -> 0f
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(daySegmentHeight)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fill)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentVerseCard(
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
    var lastWordLine by remember(text) { mutableIntStateOf(0) }
    val wordLine = highlightedWord
        ?.takeIf { word -> word.first < text.length }
        ?.let { word -> currentLayout?.getLineForOffset(word.first) }
        ?: lastWordLine
    SideEffect { lastWordLine = wordLine }
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

@Composable
private fun WordHighlightCard(
    isEnabled: Boolean,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    PlayerSection(verticalPadding = 12.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionIcon(Icons.Rounded.FormatColorText)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.listening_word_highlight),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = if (isEnabled) {
                        stringResource(Res.string.listening_word_highlight_on)
                    } else {
                        stringResource(Res.string.listening_word_highlight_off)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppSwitch(
                checked = isEnabled,
                onCheckedChange = { isChecked ->
                    onEvent(ChapterListeningPlayerUiEvent.OnWordHighlightToggle(isChecked))
                },
            )
        }
    }
}

@Composable
private fun ProgressSection(
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
                    Res.string.listening_verse_of,
                    player.verseNumber ?: 0,
                    player.verseCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    Res.string.listening_time_remaining,
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
private fun ControlsRow(
    uiState: ChapterListeningPlayerUiState,
    player: ListeningPlayerUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val isPlaying = player.status == ListeningStatusModel.PLAYING
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
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
                contentDescription = stringResource(Res.string.listening_previous_verse),
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
                        if (isPlaying) Res.string.listening_pause else Res.string.listening_play,
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
                contentDescription = stringResource(Res.string.listening_next_verse),
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
    Column(
        modifier = Modifier
            .width(chapterButtonWidth)
            .clip(RoundedCornerShape(12.dp))
            // Why: a disabled clickable with no label is announced as an unnamed control, so the first chapter's has none.
            .then(if (label == null) Modifier else Modifier.clickable(onClick = onClick))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(26.dp),
            imageVector = imageVector,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (label == null) 0.38f else 1f),
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

@Composable
private fun LockedCard(
    player: ListeningPlayerUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    PlayerSection(verticalPadding = 12.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(
                    Res.string.listening_locked_sheet,
                    chapterTitle(player.lockedChapter ?: player.chapter),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { onEvent(ChapterListeningPlayerUiEvent.OnUnlockClick) }) {
                Text(text = stringResource(Res.string.listening_unlock))
            }
        }
    }
}

@Composable
private fun UnavailableCard(
    player: ListeningPlayerUiModel,
    copy: ListeningUnavailableCopy,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    PlayerSection(verticalPadding = 12.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = copy.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(copy.title, player.versionAbbreviation),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(copy.body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            copy.action?.let { action ->
                Button(onClick = { onEvent(ChapterListeningPlayerUiEvent.OnUnavailableActionClick) }) {
                    Text(text = stringResource(action))
                }
            }
        }
    }
}

@Composable
private fun SpeedCard(
    speed: Float,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    PlayerSection(verticalPadding = 10.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionIcon(Icons.Rounded.Speed)
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.listening_speed),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            speedOptions.forEach { option ->
                FilterChip(
                    selected = option == speed,
                    onClick = { onEvent(ChapterListeningPlayerUiEvent.OnSpeedClick(option)) },
                    label = { Text(text = "${option.toSpeedLabel()}x") },
                )
            }
        }
    }
}

@Composable
private fun SleepTimerCard(
    sleepTimer: ListeningSleepTimerUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val remaining = sleepTimer.remaining
    PlayerSection(verticalPadding = 10.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionIcon(Icons.Rounded.Bedtime)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.listening_sleep_timer),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = when {
                        remaining != null -> stringResource(
                            Res.string.listening_sleep_timer_pauses_in,
                            remaining.toClockText(),
                        )

                        sleepTimer.selectedOption == ListeningSleepTimerOption.END_OF_CHAPTER ->
                            stringResource(Res.string.listening_sleep_timer_stops_at_end)

                        else -> stringResource(Res.string.listening_sleep_timer_off)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sleepTimerOptions.forEach { option ->
                FilterChip(
                    selected = option == sleepTimer.selectedOption,
                    onClick = { onEvent(ChapterListeningPlayerUiEvent.OnSleepTimerClick(option)) },
                    label = { Text(text = sleepTimerLabel(option)) },
                )
            }
        }
    }
}

@Composable
private fun AutoNextCard(
    uiState: ChapterListeningPlayerUiState,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val nextChapter = uiState.nextChapter
    PlayerSection(verticalPadding = 12.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionIcon(Icons.AutoMirrored.Rounded.PlaylistPlay)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.listening_auto_next),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = when {
                        uiState.isAutoNextLocked -> stringResource(Res.string.listening_auto_next_day)

                        uiState.isAutoNextEnabled && nextChapter != null ->
                            stringResource(Res.string.listening_auto_next_on, chapterTitle(nextChapter))

                        else -> stringResource(Res.string.listening_auto_next_off)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppSwitch(
                checked = uiState.isAutoNextEnabled,
                enabled = !uiState.isAutoNextLocked,
                onCheckedChange = { isEnabled -> onEvent(ChapterListeningPlayerUiEvent.OnAutoNextToggle(isEnabled)) },
            )
        }
    }
}

// Why: the narration is synthesized, and saying so up front keeps it from passing as a human recording.
@Composable
private fun AiVoiceLabel() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.listening_ai_voice),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlayerSection(
    verticalPadding: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 14.dp, vertical = verticalPadding),
        content = content,
    )
}

@Composable
private fun SectionIcon(imageVector: ImageVector) {
    Icon(
        modifier = Modifier.size(20.dp),
        imageVector = imageVector,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun sleepTimerLabel(option: ListeningSleepTimerOption): String = when (option) {
    ListeningSleepTimerOption.OFF -> stringResource(Res.string.listening_sleep_timer_off)
    ListeningSleepTimerOption.FIFTEEN_MINUTES -> stringResource(Res.string.listening_sleep_timer_minutes, 15)
    ListeningSleepTimerOption.THIRTY_MINUTES -> stringResource(Res.string.listening_sleep_timer_minutes, 30)
    ListeningSleepTimerOption.END_OF_CHAPTER -> stringResource(Res.string.listening_sleep_timer_end)
}

private fun Float.toSpeedLabel(): String = if (this % 1f == 0f) toInt().toString() else toString()
