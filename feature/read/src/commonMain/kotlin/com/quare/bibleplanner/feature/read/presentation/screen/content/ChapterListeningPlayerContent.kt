package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.presentation.listening.mapper.toUnavailableCopy
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.AiVoiceLabel
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.AutoNextCard
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.ControlsRow
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.CurrentVerseCard
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.DayProgressCard
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.LockedCard
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.ProgressSection
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.SleepTimerCard
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.SpeedCard
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.UnavailableCard
import com.quare.bibleplanner.feature.read.presentation.screen.content.player.WordHighlightCard

@Composable
internal fun ChapterListeningPlayerContent(
    uiState: ChapterListeningPlayerUiState,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val player = uiState.player ?: return
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        player.dayProgress?.let { DayProgressCard(dayProgress = it, progress = player.progress) }
        CurrentVerseCard(
            player = player,
            isWordHighlightEnabled = uiState.isWordHighlightEnabled,
        )
        ProgressSection(
            player = player,
            onEvent = onEvent,
        )
        ControlsRow(
            uiState = uiState,
            player = player,
            onEvent = onEvent,
        )
        if (player.status == ListeningStatusModel.NEXT_LOCKED) {
            LockedCard(
                player = player,
                onEvent = onEvent,
            )
        }
        player.status.toUnavailableCopy()?.let { copy ->
            UnavailableCard(
                player = player,
                copy = copy,
                onEvent = onEvent,
            )
        }
        SpeedCard(
            speed = uiState.speed,
            onEvent = onEvent,
        )
        SleepTimerCard(
            sleepTimer = uiState.sleepTimer,
            onEvent = onEvent,
        )
        AutoNextCard(
            uiState = uiState,
            onEvent = onEvent,
        )
        WordHighlightCard(
            isEnabled = uiState.isWordHighlightEnabled,
            onEvent = onEvent,
        )
        AiVoiceLabel()
    }
}
