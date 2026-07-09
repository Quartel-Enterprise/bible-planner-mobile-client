package com.quare.bibleplanner.feature.themeselection.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.preferences.theme_selection.generated.resources.Res
import bibleplanner.feature.preferences.theme_selection.generated.resources.select_theme
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.feature.themeselection.presentation.utils.ThemeSettingsUiActionCollector
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.themeSettings(
    onNavigate: (Any) -> Unit,
    onNavigateBack: () -> Unit,
) {
    entry<ThemeNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) {
        val viewModel = koinViewModel<ThemeSelectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        ThemeSettingsUiActionCollector(
            actionsFlow = viewModel.uiAction,
            onNavigate = onNavigate,
            onNavigateBack = onNavigateBack,
        )
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
