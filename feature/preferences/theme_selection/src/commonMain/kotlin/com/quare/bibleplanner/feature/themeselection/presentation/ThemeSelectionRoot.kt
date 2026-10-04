package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.preferences.theme_selection.generated.resources.Res
import bibleplanner.feature.preferences.theme_selection.generated.resources.select_theme
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.themeSettings() {
    entry<ThemeNavRoute>(metadata = getSheetPane()) {
        val viewModel = koinViewModel<ThemeSelectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent

        ResponsiveDialogSheet(
            onCloseClick = { onEvent(ThemeSelectionUiEvent.OnDismiss) },
            title = stringResource(Res.string.select_theme),
        ) {
            ThemeSelectionContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
