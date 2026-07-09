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
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute

@Composable
actual fun NotificationPermissionStartEffect(onNavigate: (Any) -> Unit) {
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (!isGranted) {
            val permanentlyDenied = activity?.shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS,
            ) == false
            if (permanentlyDenied) {
                onNavigate(NotificationPermissionNavRoute)
            }
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
