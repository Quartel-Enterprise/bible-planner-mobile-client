package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.inappupdate.presentation.content.InAppUpdateContent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.inAppUpdate() {
    entry<InAppUpdateNavRoute>(metadata = getSheetPane()) { route ->
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
