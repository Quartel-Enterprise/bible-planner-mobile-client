package com.quare.bibleplanner.android.feature.listeningplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiState
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private val horizontalPadding = 20.dp
private val bottomPadding = 24.dp
private val sectionSpacing = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListeningPlayerContent(
    uiState: ChapterListeningPlayerUiState,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = { sheetCloseGuard.close { onEvent(ChapterListeningPlayerUiEvent.OnDismiss) } },
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing),
        ) {
            uiState.player?.let { player ->
                ListeningPlayerHeader(player = player)
                ListeningPlayerBodyContent(
                    uiState = uiState,
                    player = player,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun ListeningPlayerHeader(player: ListeningPlayerUiModel) {
    Column(modifier = Modifier.padding(bottom = sectionSpacing)) {
        Text(
            text = chapterTitle(player.chapter),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(
                R.string.listening_player_sheet_subtitle,
                player.versionAbbreviation,
                player.verseCount,
                player.total.toClockText(),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ListeningPlayerBodyContent(
    uiState: ChapterListeningPlayerUiState,
    player: ListeningPlayerUiModel,
    onEvent: (ChapterListeningPlayerUiEvent) -> Unit,
) {
    val statusCardRequester = remember { BringIntoViewRequester() }
    val unavailableCopy = player.status.toUnavailableCopy()
    val hasStatusCard = player.status == ListeningStatusModel.NEXT_LOCKED || unavailableCopy != null
    LaunchedEffect(player.status) {
        // Why: the sheet scrolls, so a card inserted at the top while the user is scrolled down would stay out of sight.
        if (hasStatusCard) statusCardRequester.bringIntoView()
    }
    if (hasStatusCard) {
        Box(modifier = Modifier.fillMaxWidth().bringIntoViewRequester(statusCardRequester)) {
            if (player.status == ListeningStatusModel.NEXT_LOCKED) {
                LockedCard(
                    player = player,
                    onEvent = onEvent,
                )
            }
            unavailableCopy?.let { copy ->
                UnavailableCard(
                    player = player,
                    copy = copy,
                    onEvent = onEvent,
                )
            }
        }
    }
    player.dayProgress?.let { dayProgress ->
        DayProgressCard(
            dayProgress = dayProgress,
            progress = player.progress,
        )
    }
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
