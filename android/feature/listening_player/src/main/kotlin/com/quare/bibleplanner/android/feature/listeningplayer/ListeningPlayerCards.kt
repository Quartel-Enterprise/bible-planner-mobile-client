package com.quare.bibleplanner.android.feature.listeningplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningDayProgressUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.feature.read.presentation.listening.player.ListeningSleepTimerUiModel

private const val FIFTEEN_MINUTES = 15
private const val THIRTY_MINUTES = 30
private val cardShape = RoundedCornerShape(16.dp)
private val daySegmentHeight = 6.dp
private val speedOptions = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
private val sleepTimerOptions = ListeningSleepTimerOption.entries

@Composable
internal fun PlayerSection(
    verticalPadding: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(
                horizontal = 14.dp,
                vertical = verticalPadding,
            ),
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
internal fun LockedCard(
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
                    R.string.listening_player_locked_sheet,
                    chapterTitle(player.lockedChapter ?: player.chapter),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { onEvent(ChapterListeningPlayerUiEvent.OnUnlockClick) }) {
                Text(text = stringResource(R.string.listening_player_unlock))
            }
        }
    }
}

@Composable
internal fun UnavailableCard(
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
internal fun DayProgressCard(
    dayProgress: ListeningDayProgressUiModel,
    progress: Float,
) {
    PlayerSection(verticalPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.listening_player_day_title),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(
                    R.string.listening_player_day_progress,
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
internal fun SpeedCard(
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
                text = stringResource(R.string.listening_player_speed),
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

private fun Float.toSpeedLabel(): String = if (this % 1f == 0f) toInt().toString() else toString()

@Composable
internal fun SleepTimerCard(
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
                    text = stringResource(R.string.listening_player_sleep_timer),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = when {
                        remaining != null -> stringResource(
                            R.string.listening_player_sleep_timer_pauses_in,
                            remaining.toClockText(),
                        )

                        sleepTimer.selectedOption == ListeningSleepTimerOption.END_OF_CHAPTER ->
                            stringResource(R.string.listening_player_sleep_timer_stops_at_end)

                        else -> stringResource(R.string.listening_player_sleep_timer_off)
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
private fun sleepTimerLabel(option: ListeningSleepTimerOption): String = when (option) {
    ListeningSleepTimerOption.OFF -> stringResource(R.string.listening_player_sleep_timer_off)

    ListeningSleepTimerOption.FIFTEEN_MINUTES -> stringResource(
        R.string.listening_player_sleep_timer_minutes,
        FIFTEEN_MINUTES,
    )

    ListeningSleepTimerOption.THIRTY_MINUTES -> stringResource(
        R.string.listening_player_sleep_timer_minutes,
        THIRTY_MINUTES,
    )

    ListeningSleepTimerOption.END_OF_CHAPTER -> stringResource(R.string.listening_player_sleep_timer_end)
}

@Composable
internal fun AutoNextCard(
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
                    text = stringResource(R.string.listening_player_auto_next),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = when {
                        uiState.isAutoNextLocked -> stringResource(R.string.listening_player_auto_next_day)

                        uiState.isAutoNextEnabled && nextChapter != null ->
                            stringResource(R.string.listening_player_auto_next_on, chapterTitle(nextChapter))

                        else -> stringResource(R.string.listening_player_auto_next_off)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = uiState.isAutoNextEnabled,
                enabled = !uiState.isAutoNextLocked,
                onCheckedChange = { isEnabled -> onEvent(ChapterListeningPlayerUiEvent.OnAutoNextToggle(isEnabled)) },
            )
        }
    }
}

@Composable
internal fun WordHighlightCard(
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
                    text = stringResource(R.string.listening_player_word_highlight),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(
                        if (isEnabled) {
                            R.string.listening_player_word_highlight_on
                        } else {
                            R.string.listening_player_word_highlight_off
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { isChecked ->
                    onEvent(ChapterListeningPlayerUiEvent.OnWordHighlightToggle(isChecked))
                },
            )
        }
    }
}

// Why: the narration is synthesized, and saying so up front keeps it from passing as a human recording.
@Composable
internal fun AiVoiceLabel() {
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
            text = stringResource(R.string.listening_player_ai_voice),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
