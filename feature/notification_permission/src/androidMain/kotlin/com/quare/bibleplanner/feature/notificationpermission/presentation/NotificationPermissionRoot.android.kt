package com.quare.bibleplanner.feature.notificationpermission.presentation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.feature.notificationpermission.presentation.content.NotificationPermissionContent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiAction
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel.NotificationPermissionViewModel
import com.quare.bibleplanner.ui.utils.ActionCollector
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

actual fun EntryProviderScope<NavKey>.notificationPermission() {
    entry<NotificationPermissionNavRoute>(metadata = DialogSceneStrategy.dialog()) {
        val viewModel = koinViewModel<NotificationPermissionViewModel>()
        val state by viewModel.uiState.collectAsState()
        val context = LocalContext.current
        val activity = context as? ComponentActivity

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
    context: Context,
    permissionLauncher: () -> Unit,
) {
    ActionCollector(uiActionFlow) { uiAction ->
        when (uiAction) {
            NotificationPermissionUiAction.RequestSystemPermission -> permissionLauncher()
            NotificationPermissionUiAction.OpenNotificationSettings -> openNotificationSettings(context)
        }
    }
}

private fun openNotificationSettings(context: Context) {
    val notificationSettings = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    val appDetails = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    )
    // Why: some OEM builds and work profiles have no app notification settings screen, or neither screen.
    context.startFirstAvailableActivity(notificationSettings, appDetails)
}

private fun Context.startFirstAvailableActivity(vararg intents: Intent) {
    for (intent in intents) {
        try {
            startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            continue
        } catch (_: SecurityException) {
            continue
        }
    }
}
