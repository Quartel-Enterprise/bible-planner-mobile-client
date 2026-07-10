package com.quare.bibleplanner.feature.notificationpermission.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import org.koin.compose.koinInject

@Composable
actual fun NotificationPermissionStartEffect(onNavigate: (NavKey) -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED

    if (permissionGranted) return

    val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(
        Manifest.permission.POST_NOTIFICATIONS,
    ) == true

    if (shouldShowRationale) return

    val trackEvent = koinInject<TrackEvent>()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        val canAskAgain = activity?.shouldShowRequestPermissionRationale(
            Manifest.permission.POST_NOTIFICATIONS,
        ) != false
        trackEvent(
            name = AnalyticsEventNames.NOTIFICATION_PERMISSION_RESULT,
            params = mapOf(
                AnalyticsParams.IS_GRANTED to isGranted,
                AnalyticsParams.CAN_ASK_AGAIN to canAskAgain,
            ),
        )
        if (!isGranted && !canAskAgain) {
            onNavigate(NotificationPermissionNavRoute)
        }
    }

    LaunchedEffect(Unit) {
        trackEvent(
            name = AnalyticsEventNames.NOTIFICATION_PERMISSION_PROMPTED,
            params = mapOf(AnalyticsParams.IS_FIRST_TIME to true),
        )
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
