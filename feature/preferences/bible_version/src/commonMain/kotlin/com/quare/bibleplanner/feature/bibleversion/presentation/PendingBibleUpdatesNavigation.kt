package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.PendingBibleUpdatesNavRoute
import com.quare.bibleplanner.feature.bibleversion.presentation.component.PendingBibleUpdatesContent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiAction
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.utils.ActionCollector
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.pendingBibleUpdates(onNavigateBack: () -> Unit) {
    entry<PendingBibleUpdatesNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<PendingBibleUpdatesViewModel>()
        val pendingVersionNames by viewModel.pendingVersionNames.collectAsState()
        ActionCollector(viewModel.uiAction) { action ->
            when (action) {
                PendingBibleUpdatesUiAction.NavigateBack -> onNavigateBack()
            }
        }
        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(PendingBibleUpdatesUiEvent.OnDismissClick) },
        ) {
            PendingBibleUpdatesContent(
                pendingVersionNames = pendingVersionNames,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
