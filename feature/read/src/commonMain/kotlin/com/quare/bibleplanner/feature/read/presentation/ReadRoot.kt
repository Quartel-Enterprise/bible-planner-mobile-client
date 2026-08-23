package com.quare.bibleplanner.feature.read.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.reader_appearance
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.getReaderPane
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceContent
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceUiEvent
import com.quare.bibleplanner.feature.read.presentation.appearance.ReaderAppearanceViewModel
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorDialog
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorViewModel
import com.quare.bibleplanner.feature.read.presentation.screen.ReadScreen
import com.quare.bibleplanner.feature.read.presentation.utils.DeleteHighlightColorUiActionCollector
import com.quare.bibleplanner.feature.read.presentation.utils.ReadUiActionCollector
import com.quare.bibleplanner.feature.read.presentation.utils.ReaderAppearanceUiActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.read(
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    entry<ReadNavRoute>(metadata = getReaderPane()) { route ->
        val viewModel = koinViewModel<ReadViewModel> { parametersOf(route) }
        val state by viewModel.uiState.collectAsState()
        ReadUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
            onNavigateReplacingTop = onNavigateReplacingTop,
        )
        ReadScreen(
            platform = viewModel.platform,
            state = state,
            onEvent = viewModel::onEvent,
        )
    }

    entry<ReaderAppearanceNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<ReaderAppearanceViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ReaderAppearanceUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
        )
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

    entry<DeleteHighlightColorNavRoute>(metadata = DialogSceneStrategy.dialog()) { route ->
        val viewModel = koinViewModel<DeleteHighlightColorViewModel> { parametersOf(route) }
        DeleteHighlightColorUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
        )
        DeleteHighlightColorDialog(
            color = viewModel.color,
            onEvent = viewModel::onEvent,
        )
    }
}
