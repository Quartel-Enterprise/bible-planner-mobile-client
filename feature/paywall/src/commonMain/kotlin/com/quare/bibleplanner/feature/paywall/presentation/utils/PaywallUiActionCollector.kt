package com.quare.bibleplanner.feature.paywall.presentation.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString

@Composable
internal fun PaywallUiActionCollector(
    actionsFlow: Flow<PaywallUiAction>,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    ActionCollector(actionsFlow) { uiAction ->
        when (uiAction) {
            PaywallUiAction.NavigateBack -> {
                onNavigateBack()
            }

            is PaywallUiAction.NavigateTo -> {
                onNavigateReplacingTop(uiAction.route)
            }

            is PaywallUiAction.NavigateToLoginWarning -> {
                onNavigate(LoginWarningNavRoute(uiAction.reason))
            }

            is PaywallUiAction.ShowSnackbar -> {
                val message = getString(uiAction.message, *uiAction.args.toTypedArray())
                snackbarHostState.showSnackbar(message)
            }
        }
    }
}
