package com.quare.bibleplanner.feature.loginwarning.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun LoginWarningActionCollector(
    uiActionFlow: Flow<LoginWarningUiAction>,
    navController: NavController,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            LoginWarningUiAction.NavigateBack -> navController.navigateUp()

            LoginWarningUiAction.NavigateToLogin -> navController.navigate(
                LoginNavRoute(notifyResultViaSnackbar = true),
            ) {
                popUpTo<LoginWarningNavRoute> { inclusive = true }
            }
        }
    }
}
