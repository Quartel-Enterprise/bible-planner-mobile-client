package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_speed
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import org.jetbrains.compose.resources.stringResource

private val speedOptions = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)

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

private fun Float.toSpeedLabel(): String = if (this % 1f == 0f) toInt().toString() else toString()
