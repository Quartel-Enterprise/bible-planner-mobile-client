package com.quare.bibleplanner.feature.read.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ReadUiActionCollector(
    uiActionFlow: Flow<ReadUiAction>,
    navController: NavController,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            ReadUiAction.NavigateBack -> {
                navController.navigateUp()
            }

            is ReadUiAction.NavigateToRoute -> {
                navController.navigate(uiAction.route) {
                    if (uiAction.replace) {
                        navController.currentDestination?.route?.let { currentRoute ->
                            popUpTo(currentRoute) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }
}
