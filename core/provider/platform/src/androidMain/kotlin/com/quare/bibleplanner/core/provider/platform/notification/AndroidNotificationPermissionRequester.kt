package com.quare.bibleplanner.core.provider.platform.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.quare.bibleplanner.core.provider.platform.CurrentActivityProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidNotificationPermissionRequester(
    private val activityProvider: CurrentActivityProvider,
) : NotificationPermissionRequester {
    override suspend fun canPrompt(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val activity = activityProvider.activity ?: return false
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
    }

    override suspend fun request(): NotificationPermissionPromptResult = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return@withContext NotificationPermissionPromptResult.DENIED
        }
        val activity = activityProvider.activity as? ComponentActivity
            ?: return@withContext NotificationPermissionPromptResult.DENIED
        val permissionResult = CompletableDeferred<Boolean>()
        val launcher = activity.activityResultRegistry.register(
            LAUNCHER_KEY,
            ActivityResultContracts.RequestPermission(),
            permissionResult::complete,
        )
        val isGranted = try {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            permissionResult.await()
        } finally {
            launcher.unregister()
        }
        when {
            isGranted -> NotificationPermissionPromptResult.GRANTED

            activity.shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS,
            ) -> NotificationPermissionPromptResult.DENIED

            else -> NotificationPermissionPromptResult.PERMANENTLY_DENIED
        }
    }

    private companion object {
        const val LAUNCHER_KEY = "notification_permission_request"
    }
}
