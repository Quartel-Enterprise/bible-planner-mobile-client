package com.quare.bibleplanner.feature.themeselection.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ThemeSettingsUiActionCollector(
    actionsFlow: Flow<ThemeSelectionUiAction>,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    ActionCollector(actionsFlow) { uiAction ->
        when (uiAction) {
            ThemeSelectionUiAction.NavigateBack -> onNavigateBack()

            ThemeSelectionUiAction.NavigateToMaterialYou -> onNavigate(MaterialYouBottomSheetNavRoute)

            ThemeSelectionUiAction.NavigateToLoginWarning ->
                onNavigate(LoginWarningNavRoute(LoginWarningReason.Preferences.Theme.key))
        }
    }
}
