package com.quare.bibleplanner.feature.notificationpermission.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.feature.notificationpermission.presentation.content.NotificationPermissionContent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiAction
import com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel.NotificationPermissionViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
actual fun NavGraphBuilder.notificationPermission(navController: NavController) {
    dialog<NotificationPermissionNavRoute> {
        val viewModel = koinViewModel<NotificationPermissionViewModel>()
        val state by viewModel.uiState.collectAsState()

        NotificationPermissionUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navController,
        )

        NotificationPermissionContent(
            uiState = state,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun NotificationPermissionUiActionCollector(
    uiActionFlow: Flow<NotificationPermissionUiAction>,
    navController: NavController,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            NotificationPermissionUiAction.RequestSystemPermission -> {
                // iOS handles notification permissions through system settings
                navController.navigateUp()
            }

            NotificationPermissionUiAction.NavigateBack -> {
                navController.navigateUp()
            }

            NotificationPermissionUiAction.OpenNotificationSettings -> {
                // iOS handles notification permissions through system settings
                navController.navigateUp()
            }
        }
    }
}
