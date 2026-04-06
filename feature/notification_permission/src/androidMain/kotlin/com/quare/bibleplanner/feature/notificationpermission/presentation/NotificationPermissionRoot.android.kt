package com.quare.bibleplanner.feature.notificationpermission.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.feature.notificationpermission.presentation.content.NotificationPermissionContent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiAction
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel.NotificationPermissionViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
actual fun NavGraphBuilder.notificationPermission(navController: NavController) {
    dialog<NotificationPermissionNavRoute> {
        val viewModel = koinViewModel<NotificationPermissionViewModel>()
        val state by viewModel.uiState.collectAsState()
        val context = remember { navController.context }
        val activity = context as? ComponentActivity
        val navHostController = navController as NavHostController

        // This dialog is only navigated to when permission is permanently denied —
        // skip the initial "Allow" state and go straight to "Open Settings".
        LaunchedEffect(Unit) {
            viewModel.onEvent(
                NotificationPermissionUiEvent.OnPermissionResult(
                    granted = false,
                    canAskAgain = false,
                ),
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            val canAskAgain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity?.shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == true
            } else {
                true
            }
            viewModel.onEvent(
                NotificationPermissionUiEvent.OnPermissionResult(
                    granted = isGranted,
                    canAskAgain = canAskAgain,
                ),
            )
        }

        NotificationPermissionUiActionCollector(
            uiActionFlow = viewModel.uiAction,
            navController = navHostController,
            context = context,
            permissionLauncher = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
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
    navController: NavHostController,
    context: android.content.Context,
    permissionLauncher: () -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            NotificationPermissionUiAction.RequestSystemPermission -> permissionLauncher()
            NotificationPermissionUiAction.NavigateBack -> navController.navigateUp()
            NotificationPermissionUiAction.OpenNotificationSettings -> {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        }
    }
}
