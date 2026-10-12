package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_next_verse
import bibleplanner.feature.read.generated.resources.listening_pause
import bibleplanner.feature.read.generated.resources.listening_play
import bibleplanner.feature.read.generated.resources.listening_previous_verse
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.feature.read.presentation.screen.component.chapterTitle
import org.jetbrains.compose.resources.stringResource

private val playButtonSize = 68.dp
private val verseButtonSize = 52.dp
private val chapterButtonWidth = 64.dp

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
