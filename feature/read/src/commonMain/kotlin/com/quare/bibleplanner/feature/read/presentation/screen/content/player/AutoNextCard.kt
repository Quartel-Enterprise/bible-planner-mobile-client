package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_auto_next
import bibleplanner.feature.read.generated.resources.listening_auto_next_day
import bibleplanner.feature.read.generated.resources.listening_auto_next_off
import bibleplanner.feature.read.generated.resources.listening_auto_next_on
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.feature.read.presentation.screen.component.chapterTitle
import com.quare.bibleplanner.ui.component.AppSwitch
import org.jetbrains.compose.resources.stringResource

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
