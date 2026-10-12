package com.quare.bibleplanner.feature.read.presentation.screen.content.player

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_sleep_timer
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_end
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_minutes
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_off
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_pauses_in
import bibleplanner.feature.read.generated.resources.listening_sleep_timer_stops_at_end
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ListeningSleepTimerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.toClockText
import org.jetbrains.compose.resources.stringResource

private val sleepTimerOptions = ListeningSleepTimerOption.entries

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
private fun sleepTimerLabel(option: ListeningSleepTimerOption): String = when (option) {
    ListeningSleepTimerOption.OFF -> stringResource(Res.string.listening_sleep_timer_off)
    ListeningSleepTimerOption.FIFTEEN_MINUTES -> stringResource(Res.string.listening_sleep_timer_minutes, 15)
    ListeningSleepTimerOption.THIRTY_MINUTES -> stringResource(Res.string.listening_sleep_timer_minutes, 30)
    ListeningSleepTimerOption.END_OF_CHAPTER -> stringResource(Res.string.listening_sleep_timer_end)
}
