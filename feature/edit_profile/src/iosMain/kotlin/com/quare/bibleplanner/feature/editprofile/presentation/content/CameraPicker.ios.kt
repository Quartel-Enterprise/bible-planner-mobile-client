package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelAlert
import platform.UIKit.UIWindowScene

@Composable
actual fun rememberCameraPicker(onResult: (PlatformFile?) -> Unit): () -> Unit {
    val presenterWindow = remember { CameraPresenterWindow() }
    val launcher = rememberCameraPickerLauncher(
        openCameraSettings = FileKitOpenCameraSettings(presenter = presenterWindow.hostViewController),
        onError = {
            presenterWindow.detach()
            onResult(null)
        },
        onResult = { file ->
            presenterWindow.detach()
            onResult(file)
        },
    )
    return {
        presenterWindow.attach()
        launcher.launch(cameraFacing = FileKitCameraFacing.Front)
    }
}

private class CameraPresenterWindow {
    val hostViewController = UIViewController()
    private var window: UIWindow? = null
    private var previousKeyWindow: UIWindow? = null

    @OptIn(ExperimentalForeignApi::class)
    fun attach() {
        val application = UIApplication.sharedApplication
        previousKeyWindow = application.keyWindow
        val scene = application.connectedScenes.firstNotNullOfOrNull { it as? UIWindowScene }
        val newWindow = if (scene != null) {
            UIWindow(windowScene = scene)
        } else {
            UIWindow(frame = UIScreen.mainScreen.bounds)
        }
        newWindow.rootViewController = hostViewController
        newWindow.windowLevel = UIWindowLevelAlert + 1.0
        newWindow.backgroundColor = UIColor.clearColor
        newWindow.makeKeyAndVisible()
        window = newWindow
    }

    fun detach() {
        window?.setHidden(true)
        window?.rootViewController = null
        window = null
        previousKeyWindow?.makeKeyAndVisible()
        previousKeyWindow = null
    }
}
