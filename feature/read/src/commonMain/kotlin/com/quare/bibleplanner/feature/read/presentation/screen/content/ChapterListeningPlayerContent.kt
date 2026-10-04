package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VoiceOverOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_auto_next
import bibleplanner.feature.read.generated.resources.listening_auto_next_day
import bibleplanner.feature.read.generated.resources.listening_auto_next_off
import bibleplanner.feature.read.generated.resources.listening_auto_next_on
import bibleplanner.feature.read.generated.resources.listening_day_progress
import bibleplanner.feature.read.generated.resources.listening_day_title
import bibleplanner.feature.read.generated.resources.listening_enhanced_hint
import bibleplanner.feature.read.generated.resources.listening_enhanced_hint_path
import bibleplanner.feature.read.generated.resources.listening_locked_sheet
import bibleplanner.feature.read.generated.resources.listening_next_verse
import bibleplanner.feature.read.generated.resources.listening_open_device_settings
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
import bibleplanner.feature.read.generated.resources.listening_voice
import bibleplanner.feature.read.generated.resources.listening_voice_enhanced
import bibleplanner.feature.read.generated.resources.listening_voice_fallback_name
import bibleplanner.feature.read.generated.resources.listening_voice_none
import bibleplanner.feature.read.generated.resources.listening_voice_preview
import bibleplanner.feature.read.generated.resources.listening_voice_standard
import bibleplanner.feature.read.generated.resources.listening_voice_unavailable_sheet_body
import bibleplanner.feature.read.generated.resources.listening_voice_unavailable_sheet_title
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningDayProgressUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.feature.read.presentation.listening.player.ListeningSleepTimerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ListeningVoiceOptionUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ListeningVoicesUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.toClockText
import com.quare.bibleplanner.feature.read.presentation.listening.toLanguageNameResource
import com.quare.bibleplanner.feature.read.presentation.screen.component.chapterTitle
import com.quare.bibleplanner.feature.read.presentation.screen.component.verseReference
import com.quare.bibleplanner.ui.component.AppSwitch
import com.quare.bibleplanner.ui.theme.font.displaySerifFontFamily
import org.jetbrains.compose.resources.stringResource

private const val SOFT_PRIMARY_ALPHA = 0.12f
private const val VERSE_MAX_LINES = 3
private const val CHEVRON_EXPANDED_DEGREES = 180f
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
        CurrentVerseCard(player = player)
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
        SpeedCard(
            speed = uiState.speed,
            onEvent = onEvent,
        )
        VoiceCard(
            voices = uiState.voices,
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
private fun CurrentVerseCard(player: ListeningPlayerUiModel) {
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
            .clickable(
                enabled = label != null,
                onClick = onClick,
            ).padding(vertical = 4.dp),
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
private fun VoiceCard(
    voices: ListeningVoicesUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(voices is ListeningVoicesUiModel.Unavailable) }
    val chevronRotation by animateFloatAsState(if (isExpanded) CHEVRON_EXPANDED_DEGREES else 0f)
    val selectedVoice = (voices as? ListeningVoicesUiModel.Available)?.options?.find { it.isSelected }
    PlayerSection(verticalPadding = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionIcon(Icons.Rounded.RecordVoiceOver)
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.listening_voice),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = selectedVoice?.let { voiceName(it) } ?: stringResource(Res.string.listening_voice_none),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                modifier = Modifier.rotate(chevronRotation),
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                when (voices) {
                    ListeningVoicesUiModel.Loading -> Box(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }

                    is ListeningVoicesUiModel.Unavailable -> VoiceUnavailableSection(
                        languageTag = voices.languageTag,
                        onEvent = onEvent,
                    )

                    is ListeningVoicesUiModel.Available -> {
                        voices.options.forEach { voice ->
                            VoiceOptionRow(
                                voice = voice,
                                onEvent = onEvent,
                            )
                        }
                        if (voices.shouldSuggestEnhancedVoice) {
                            EnhancedVoiceHintRow(onEvent = onEvent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceUnavailableSection(
    languageTag: String,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val language = stringResource(languageTag.toLanguageNameResource())
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.VoiceOverOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.listening_voice_unavailable_sheet_title, language),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = stringResource(Res.string.listening_voice_unavailable_sheet_body, language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { onEvent(ChapterListeningPlayerUiEvent.OnVoiceSettingsClick) },
            ) {
                Icon(
                    modifier = Modifier.size(17.dp),
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 6.dp),
                    text = stringResource(Res.string.listening_open_device_settings),
                )
            }
        }
    }
}

@Composable
private fun VoiceOptionRow(
    voice: ListeningVoiceOptionUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (voice.isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            ).clickable { onEvent(ChapterListeningPlayerUiEvent.OnVoiceClick(voice)) }
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = if (voice.isSelected) {
                Icons.Rounded.RadioButtonChecked
            } else {
                Icons.Rounded.RadioButtonUnchecked
            },
            contentDescription = null,
            tint = if (voice.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = voiceName(voice),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    if (voice.isEnhanced) Res.string.listening_voice_enhanced else Res.string.listening_voice_standard,
                    voice.languageTag,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            modifier = Modifier.height(32.dp),
            onClick = { onEvent(ChapterListeningPlayerUiEvent.OnVoicePreviewClick(voice)) },
            contentPadding = PaddingValues(start = 8.dp, end = 10.dp),
        ) {
            Icon(
                modifier = Modifier.size(17.dp),
                imageVector = if (voice.isPreviewing) Icons.Rounded.GraphicEq else Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = stringResource(Res.string.listening_voice_preview),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun EnhancedVoiceHintRow(onEvent: (ChapterListeningPlayerUiEvent) -> Unit) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.listening_enhanced_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onEvent(ChapterListeningPlayerUiEvent.OnVoiceSettingsClick) }
                    .padding(vertical = 4.dp),
                text = stringResource(Res.string.listening_enhanced_hint_path),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
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
private fun voiceName(voice: ListeningVoiceOptionUiModel): String =
    voice.name.ifBlank { stringResource(Res.string.listening_voice_fallback_name, voice.position) }

@Composable
private fun sleepTimerLabel(option: ListeningSleepTimerOption): String = when (option) {
    ListeningSleepTimerOption.OFF -> stringResource(Res.string.listening_sleep_timer_off)
    ListeningSleepTimerOption.FIFTEEN_MINUTES -> stringResource(Res.string.listening_sleep_timer_minutes, 15)
    ListeningSleepTimerOption.THIRTY_MINUTES -> stringResource(Res.string.listening_sleep_timer_minutes, 30)
    ListeningSleepTimerOption.END_OF_CHAPTER -> stringResource(Res.string.listening_sleep_timer_end)
}

private fun Float.toSpeedLabel(): String = if (this % 1f == 0f) toInt().toString() else toString()
