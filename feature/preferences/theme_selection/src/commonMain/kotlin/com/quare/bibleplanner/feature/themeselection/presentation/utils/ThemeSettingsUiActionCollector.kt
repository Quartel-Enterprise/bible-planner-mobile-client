package com.quare.bibleplanner.feature.themeselection.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ThemeSettingsUiActionCollector(
    actionsFlow: Flow<ThemeSelectionUiAction>,
    navController: NavHostController,
) {
    ActionCollector(actionsFlow) { uiAction ->
        navController.run {
            when (uiAction) {
                ThemeSelectionUiAction.NavigateBack -> navigateUp()
                ThemeSelectionUiAction.NavigateToMaterialYou -> navigate(MaterialYouBottomSheetNavRoute)
            }
        }
    }
}
