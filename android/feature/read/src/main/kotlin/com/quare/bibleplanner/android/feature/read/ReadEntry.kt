package com.quare.bibleplanner.android.feature.read

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.getReaderPane
import com.quare.bibleplanner.feature.read.presentation.ReadViewModel
import com.quare.bibleplanner.feature.read.presentation.listening.ReadListeningViewModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiAction
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun ReadNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = getReaderPane(),
) {
    val viewModel = koinViewModel<ReadViewModel> { parametersOf(this) }
    val listeningViewModel = koinViewModel<ReadListeningViewModel> { parametersOf(this) }
    val state by viewModel.uiState.collectAsState()
    val listeningState by listeningViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(listeningViewModel.uiAction) { uiAction ->
        when (uiAction) {
            ReadListeningUiAction.ShowSleepTimerEnded -> appSnackbarController.show(
                AppSnackbarMessage.Text(
                    text = context.getString(R.string.read_listening_sleep_timer_ended),
                    isDismissible = true,
                ),
            )
        }
    }
    val isWindowWide = LocalIsWideLayout.current
    LaunchedEffect(isWindowWide) {
        viewModel.onEvent(ReadUiEvent.OnWidthClassChanged(isWindowWide))
    }
    ExpressiveTheme {
        ReaderWidthLayout {
            ReadContent(
                state = state,
                listening = listeningState,
                onEvent = viewModel::onEvent,
                onListeningEvent = listeningViewModel::onEvent,
            )
        }
    }
}
