package com.quare.bibleplanner.feature.read.presentation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_sheet_subtitle
import bibleplanner.feature.read.generated.resources.reader_appearance
import com.quare.bibleplanner.core.model.route.ChapterListeningPlayerNavRoute
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.getReaderPane
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceContent
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceUiEvent
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceViewModel
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorDialog
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorViewModel
import com.quare.bibleplanner.feature.read.presentation.listening.ReadListeningViewModel
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.player.ChapterListeningPlayerViewModel
import com.quare.bibleplanner.feature.read.presentation.listening.toClockText
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.screen.ReadScreen
import com.quare.bibleplanner.feature.read.presentation.screen.component.ReaderWidthLayout
import com.quare.bibleplanner.feature.read.presentation.screen.component.chapterTitle
import com.quare.bibleplanner.feature.read.presentation.screen.content.ChapterListeningPlayerContent
import com.quare.bibleplanner.feature.read.presentation.utils.DeleteHighlightColorUiActionCollector
import com.quare.bibleplanner.feature.read.presentation.utils.ReadListeningUiActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.component.dialog.toNativeAlertDialogProperties
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.read(dayCompletionBanner: DayCompletionBannerSlot) {
    entry<ReadNavRoute>(metadata = getReaderPane()) { route ->
        val viewModel = koinViewModel<ReadViewModel> { parametersOf(route) }
        val listeningViewModel = koinViewModel<ReadListeningViewModel> { parametersOf(route) }
        val state by viewModel.uiState.collectAsState()
        val listeningState by listeningViewModel.uiState.collectAsState()
        ReadListeningUiActionCollector(uiActionFlow = listeningViewModel.uiAction)
        val isWindowWide = LocalIsWideLayout.current
        LaunchedEffect(isWindowWide) {
            viewModel.onEvent(ReadUiEvent.OnWidthClassChanged(isWindowWide))
        }
        ReaderWidthLayout {
            ReadScreen(
                platform = viewModel.platform,
                state = state,
                listening = listeningState,
                onEvent = viewModel::onEvent,
                onListeningEvent = listeningViewModel::onEvent,
                dayCompletionBanner = dayCompletionBanner,
            )
        }
    }

    entry<ReaderAppearanceNavRoute>(metadata = getSheetPane()) {
        val viewModel = koinViewModel<ReaderAppearanceViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(ReaderAppearanceUiEvent.OnDismiss) },
            title = stringResource(Res.string.reader_appearance),
            isTitleCentred = true,
            sheetBottomBreathingRoom = 12.dp,
        ) {
            ReaderAppearanceContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }

    entry<ChapterListeningPlayerNavRoute>(metadata = getSheetPane()) {
        val viewModel = koinViewModel<ChapterListeningPlayerViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val player = uiState.player
        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(ChapterListeningPlayerUiEvent.OnDismiss) },
            title = player?.let { chapterTitle(it.chapter) },
            subtitle = player?.let {
                stringResource(
                    Res.string.listening_sheet_subtitle,
                    it.versionAbbreviation,
                    it.verseCount,
                    it.total.toClockText(),
                )
            },
            sheetBottomBreathingRoom = 12.dp,
        ) {
            ChapterListeningPlayerContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
            )
        }
    }

    entry<DeleteHighlightColorNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties().toNativeAlertDialogProperties()),
    ) { route ->
        val viewModel = koinViewModel<DeleteHighlightColorViewModel> { parametersOf(route) }
        DeleteHighlightColorUiActionCollector(uiActionFlow = viewModel.uiAction)
        DeleteHighlightColorDialog(
            color = viewModel.color,
            onEvent = viewModel::onEvent,
        )
    }
}
