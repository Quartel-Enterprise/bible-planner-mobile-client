package com.quare.bibleplanner.feature.notificationpermission.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.feature.notificationpermission.presentation.content.NotificationPermissionContent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiAction
import com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel.NotificationPermissionViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

actual fun EntryProviderScope<NavKey>.notificationPermission(onNavigateBack: () -> Unit) {
    entry<NotificationPermissionNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<NotificationPermissionViewModel>()
        val state by viewModel.uiState.collectAsState()

        NotificationPermissionUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            onNavigateBack = onNavigateBack,
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
    onNavigateBack: () -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            NotificationPermissionUiAction.RequestSystemPermission -> {
                onNavigateBack()
            }

            NotificationPermissionUiAction.NavigateBack -> {
                onNavigateBack()
            }

            NotificationPermissionUiAction.OpenNotificationSettings -> {
                onNavigateBack()
            }
        }
    }
}
