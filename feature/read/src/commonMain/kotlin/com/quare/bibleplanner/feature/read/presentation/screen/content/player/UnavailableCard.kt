package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningUnavailableCopy
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import org.jetbrains.compose.resources.stringResource

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
