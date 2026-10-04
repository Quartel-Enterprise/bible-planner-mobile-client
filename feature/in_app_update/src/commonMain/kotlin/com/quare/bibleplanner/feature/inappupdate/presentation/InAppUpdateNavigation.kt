package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.feature.inappupdate.presentation.content.InAppUpdateContent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import com.quare.bibleplanner.ui.component.dialog.toSheetDialogProperties
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.inAppUpdate() {
    entry<InAppUpdateNavRoute>(
        metadata = DialogSceneStrategy.dialog(
            DialogProperties(usePlatformDefaultWidth = false).toSheetDialogProperties(),
        ),
    ) { route ->
        val viewModel = koinViewModel<InAppUpdateViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()
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
