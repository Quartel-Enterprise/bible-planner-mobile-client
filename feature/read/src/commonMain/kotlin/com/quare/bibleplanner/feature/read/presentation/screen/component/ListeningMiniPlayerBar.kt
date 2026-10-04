package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.VoiceOverOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_close
import bibleplanner.feature.read.generated.resources.listening_interrupted_title
import bibleplanner.feature.read.generated.resources.listening_locked_body
import bibleplanner.feature.read.generated.resources.listening_locked_title
import bibleplanner.feature.read.generated.resources.listening_mini_day
import bibleplanner.feature.read.generated.resources.listening_mini_finished
import bibleplanner.feature.read.generated.resources.listening_mini_paused
import bibleplanner.feature.read.generated.resources.listening_mini_playing
import bibleplanner.feature.read.generated.resources.listening_next_verse
import bibleplanner.feature.read.generated.resources.listening_pause
import bibleplanner.feature.read.generated.resources.listening_paused_at
import bibleplanner.feature.read.generated.resources.listening_play
import bibleplanner.feature.read.generated.resources.listening_preparing
import bibleplanner.feature.read.generated.resources.listening_resume
import bibleplanner.feature.read.generated.resources.listening_settings
import bibleplanner.feature.read.generated.resources.listening_unlock
import bibleplanner.feature.read.generated.resources.listening_voice_unavailable_body
import bibleplanner.feature.read.generated.resources.listening_voice_unavailable_title
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.toClockText
import com.quare.bibleplanner.feature.read.presentation.listening.toLanguageNameResource
import org.jetbrains.compose.resources.stringResource

internal const val LISTENING_MINI_PLAYER_TAG = "listening_mini_player"
private const val SOFT_PRIMARY_ALPHA = 0.12f
private val barHeight = 64.dp
private val progressHeight = 3.dp
private val leadingSize = 40.dp
private val actionButtonHeight = 34.dp
private val contentMaxWidth = 640.dp

@Composable
internal fun ListeningMiniPlayerBar(
    player: ListeningPlayerUiModel,
    onEvent: (ReadListeningUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(targetValue = player.progress)
    Column(
        modifier = modifier
            .testTag(LISTENING_MINI_PLAYER_TAG)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(progressHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = contentMaxWidth)
                .fillMaxWidth()
                .height(barHeight)
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            when (player.status) {
                ListeningStatusModel.PREPARING -> PreparingContent(player, onEvent)

                ListeningStatusModel.VOICE_UNAVAILABLE -> VoiceUnavailableContent(player, onEvent)

                ListeningStatusModel.INTERRUPTED -> InterruptedContent(player, onEvent)

                ListeningStatusModel.NEXT_LOCKED -> LockedContent(player, onEvent)

                ListeningStatusModel.PLAYING,
                ListeningStatusModel.PAUSED,
                ListeningStatusModel.FINISHED,
                -> PlaybackContent(player, onEvent)
            }
            IconButton(onClick = { onEvent(ReadListeningUiEvent.OnCloseClick) }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.listening_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RowScope.PlaybackContent(
    player: ListeningPlayerUiModel,
    onEvent: (ReadListeningUiEvent) -> Unit,
) {
    val isPlaying = player.status == ListeningStatusModel.PLAYING
    FilledIconButton(
        modifier = Modifier.size(leadingSize),
        onClick = { onEvent(ReadListeningUiEvent.OnPlayPauseClick) },
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = stringResource(
                if (isPlaying) Res.string.listening_pause else Res.string.listening_play,
            ),
        )
    }
    val dayProgress = player.dayProgress
    val subtitle = when {
        player.status == ListeningStatusModel.FINISHED -> stringResource(Res.string.listening_mini_finished)

        dayProgress != null -> stringResource(
            Res.string.listening_mini_day,
            dayProgress.currentIndex + 1,
            dayProgress.chapters.size,
            player.remaining.toClockText(),
        )

        isPlaying -> stringResource(
            Res.string.listening_mini_playing,
            player.versionAbbreviation,
            player.remaining.toClockText(),
        )

        else -> stringResource(Res.string.listening_mini_paused, player.remaining.toClockText())
    }
    PlayerTitleColumn(
        title = verseReference(
            chapter = player.chapter,
            verseNumber = player.verseNumber,
        ),
        subtitle = subtitle,
        isAnimating = isPlaying,
        onEvent = onEvent,
    )
    IconButton(onClick = { onEvent(ReadListeningUiEvent.OnNextVerseClick) }) {
        Icon(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = stringResource(Res.string.listening_next_verse),
        )
    }
}

@Composable
private fun RowScope.PreparingContent(
    player: ListeningPlayerUiModel,
    onEvent: (ReadListeningUiEvent) -> Unit,
) {
    LeadingBox(color = MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.5.dp,
        )
    }
    PlayerTitleColumn(
        title = stringResource(Res.string.listening_preparing),
        subtitle = "${chapterTitle(player.chapter)} · ${player.versionAbbreviation}",
        isAnimating = null,
        onEvent = onEvent,
    )
}

@Composable
private fun RowScope.VoiceUnavailableContent(
    player: ListeningPlayerUiModel,
    onEvent: (ReadListeningUiEvent) -> Unit,
) {
    val language = stringResource(player.languageTag.toLanguageNameResource())
    LeadingIcon(
        imageVector = Icons.Rounded.VoiceOverOff,
        tint = MaterialTheme.colorScheme.tertiary,
    )
    PlayerTitleColumn(
        title = stringResource(Res.string.listening_voice_unavailable_title, language),
        subtitle = stringResource(Res.string.listening_voice_unavailable_body, language),
        isAnimating = null,
        onEvent = onEvent,
    )
    OutlinedButton(
        modifier = Modifier.height(actionButtonHeight),
        onClick = { onEvent(ReadListeningUiEvent.OnVoiceSettingsClick) },
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        Text(text = stringResource(Res.string.listening_settings))
    }
}

@Composable
private fun RowScope.InterruptedContent(
    player: ListeningPlayerUiModel,
    onEvent: (ReadListeningUiEvent) -> Unit,
) {
    LeadingBox(color = MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)) {
        IconButton(onClick = { onEvent(ReadListeningUiEvent.OnPlayPauseClick) }) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = stringResource(Res.string.listening_play),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
    PlayerTitleColumn(
        title = stringResource(Res.string.listening_interrupted_title),
        subtitle = stringResource(
            Res.string.listening_paused_at,
            verseReference(
                chapter = player.chapter,
                verseNumber = player.verseNumber,
            ),
        ),
        isAnimating = null,
        onEvent = onEvent,
    )
    Button(
        modifier = Modifier.height(actionButtonHeight),
        onClick = { onEvent(ReadListeningUiEvent.OnPlayPauseClick) },
        contentPadding = PaddingValues(horizontal = 14.dp),
    ) {
        Text(text = stringResource(Res.string.listening_resume))
    }
}

@Composable
private fun RowScope.LockedContent(
    player: ListeningPlayerUiModel,
    onEvent: (ReadListeningUiEvent) -> Unit,
) {
    val lockedChapter = player.lockedChapter ?: player.chapter
    LeadingIcon(
        imageVector = Icons.Rounded.Lock,
        tint = MaterialTheme.colorScheme.primary,
    )
    PlayerTitleColumn(
        title = stringResource(Res.string.listening_locked_title, chapterTitle(lockedChapter)),
        subtitle = stringResource(Res.string.listening_locked_body),
        isAnimating = null,
        onEvent = onEvent,
    )
    Button(
        modifier = Modifier.height(actionButtonHeight),
        onClick = { onEvent(ReadListeningUiEvent.OnUnlockNextClick) },
        contentPadding = PaddingValues(horizontal = 14.dp),
    ) {
        Text(text = stringResource(Res.string.listening_unlock))
    }
}

@Composable
private fun LeadingBox(
    color: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(leadingSize)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun LeadingIcon(
    imageVector: ImageVector,
    tint: Color,
) {
    LeadingBox(color = tint.copy(alpha = SOFT_PRIMARY_ALPHA)) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
private fun RowScope.PlayerTitleColumn(
    title: String,
    subtitle: String,
    isAnimating: Boolean?,
    onEvent: (ReadListeningUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onEvent(ReadListeningUiEvent.OnMiniPlayerClick) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.weight(1f, fill = false),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            isAnimating?.let { ListeningEqualizerIndicator(isAnimating = it) }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
