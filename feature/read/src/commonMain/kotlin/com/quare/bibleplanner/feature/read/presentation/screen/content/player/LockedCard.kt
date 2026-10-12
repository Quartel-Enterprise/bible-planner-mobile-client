package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_locked_sheet
import bibleplanner.feature.read.generated.resources.listening_unlock
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.screen.component.chapterTitle
import org.jetbrains.compose.resources.stringResource

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
