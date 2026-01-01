package com.quare.bibleplanner.feature.unlockpremium.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun UnlockPremiumUiActionCollector(
    actionsFlow: Flow<UnlockPremiumUiAction>,
    navController: NavHostController,
) {
    ActionCollector(actionsFlow) { uiAction ->
        when (uiAction) {
            UnlockPremiumUiAction.NavigateBack -> {
                navController.navigateUp()
            }
        }
    }
}
