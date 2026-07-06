package com.quare.bibleplanner.feature.loginsyncnudge.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun LoginSyncNudgeActionCollector(
    uiActionFlow: Flow<LoginSyncNudgeUiAction>,
    navController: NavController,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            LoginSyncNudgeUiAction.Dismiss -> navController.navigateUp()

            LoginSyncNudgeUiAction.NavigateToLogin -> navController.navigate(
                LoginNavRoute(notifyResultViaSnackbar = true),
            ) {
                popUpTo<LoginSyncNudgeNavRoute> { inclusive = true }
            }
        }
    }
}
