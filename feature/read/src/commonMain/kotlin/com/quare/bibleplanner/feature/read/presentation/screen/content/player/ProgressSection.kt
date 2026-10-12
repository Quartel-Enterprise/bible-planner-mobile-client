package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_time_remaining
import bibleplanner.feature.read.generated.resources.listening_verse_of
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.toClockText
import org.jetbrains.compose.resources.stringResource

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
