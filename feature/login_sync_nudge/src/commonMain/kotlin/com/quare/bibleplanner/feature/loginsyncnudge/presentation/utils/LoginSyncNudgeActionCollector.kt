package com.quare.bibleplanner.feature.loginsyncnudge.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiAction
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow

@Composable
internal fun LoginSyncNudgeActionCollector(
    uiActionFlow: Flow<LoginSyncNudgeUiAction>,
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
) {
    ActionCollector(uiActionFlow) { action ->
        when (action) {
            LoginSyncNudgeUiAction.Dismiss -> onNavigateBack()

            LoginSyncNudgeUiAction.NavigateToLogin -> onNavigateReplacingTop(
                LoginNavRoute(notifyResultViaSnackbar = true),
            )
        }
    }
}
