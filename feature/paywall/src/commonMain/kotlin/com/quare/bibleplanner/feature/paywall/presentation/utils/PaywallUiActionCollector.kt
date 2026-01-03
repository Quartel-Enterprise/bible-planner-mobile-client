package com.quare.bibleplanner.feature.paywall.presentation.utils

import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.quare.bibleplanner.core.model.route.OnboardingStartDateNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun PaywallUiActionCollector(
    actionsFlow: Flow<PaywallUiAction>,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    ActionCollector(actionsFlow) { uiAction ->
        when (uiAction) {
            PaywallUiAction.NavigateBack -> {
                navController.navigateUp()
            }
            is PaywallUiAction.NavigateTo -> {
                navController.navigate(uiAction.route) {
                    popUpTo(PaywallNavRoute) { inclusive = true }
                }
            }
            is PaywallUiAction.ShowSnackbar -> {
                val message = getString(uiAction.message)
                snackbarHostState.showSnackbar(message)
            }
        }
    }
}
