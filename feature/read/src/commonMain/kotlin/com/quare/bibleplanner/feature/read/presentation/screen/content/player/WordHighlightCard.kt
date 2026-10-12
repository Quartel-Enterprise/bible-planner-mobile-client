package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_word_highlight
import bibleplanner.feature.read.generated.resources.listening_word_highlight_off
import bibleplanner.feature.read.generated.resources.listening_word_highlight_on
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.ui.component.AppSwitch
import org.jetbrains.compose.resources.stringResource

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
