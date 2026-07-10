package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.feature.inappupdate.presentation.content.InAppUpdateContent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.utils.InAppUpdateUiActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.inAppUpdate(onNavigateBack: () -> Unit) {
    entry<InAppUpdateNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        val viewModel = koinViewModel<InAppUpdateViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()
        InAppUpdateUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
        )
        ResponsiveDialogSheet(
            onCloseClick = { viewModel.onEvent(InAppUpdateUiEvent.OnDismiss) },
        ) {
            InAppUpdateContent(
                state = uiState,
                onEvent = viewModel::onEvent,
            )
        }
    }
}
