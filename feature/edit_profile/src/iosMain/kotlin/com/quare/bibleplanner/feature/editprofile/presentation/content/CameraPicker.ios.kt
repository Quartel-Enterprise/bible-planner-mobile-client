package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleLarge
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraDevice
import platform.UIKit.UIImagePickerControllerCameraFlashModeOff
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelAlert
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import kotlin.time.Duration.Companion.milliseconds

private const val JPEG_QUALITY = 0.9
private const val SCRIM_ALPHA = 0.35

@Composable
actual fun rememberCameraPicker(onResult: (PlatformFile?) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val picker = remember { IosCameraPicker() }
    return {
        picker.launch(
            scope = scope,
            onResult = onResult,
        )
    }
}

private class IosCameraPicker {
    private val cameraSourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    private val presentDelay = 50.milliseconds
    private val hostViewController = UIViewController()
    private val spinner = UIActivityIndicatorView(activityIndicatorStyle = UIActivityIndicatorViewStyleLarge).apply {
        color = UIColor.whiteColor
        translatesAutoresizingMaskIntoConstraints = false
        hostViewController.view.addSubview(this)
        centerXAnchor.constraintEqualToAnchor(hostViewController.view.centerXAnchor).active = true
        centerYAnchor.constraintEqualToAnchor(hostViewController.view.centerYAnchor).active = true
    }
    private val frontCamera = UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceFront
    private val pickerController by lazy {
        UIImagePickerController().apply {
            sourceType = cameraSourceType
            cameraFlashMode = UIImagePickerControllerCameraFlashModeOff
            if (UIImagePickerController.isCameraDeviceAvailable(frontCamera)) {
                cameraDevice = frontCamera
            }
        }
    }
    private var window: UIWindow? = null
    private var previousKeyWindow: UIWindow? = null
    private var delegate: CameraDelegate? = null

    fun launch(
        scope: CoroutineScope,
        onResult: (PlatformFile?) -> Unit,
    ) {
        if (!UIImagePickerController.isSourceTypeAvailable(cameraSourceType)) {
            onResult(null)
            return
        }
        showWindow()
        spinner.startAnimating()
        delegate = CameraDelegate { image ->
            tearDownWindow()
            scope.launch {
                onResult(image?.let { encodeToTempFile(it) })
            }
        }
        scope.launch {
            delay(presentDelay)
            pickerController.delegate = delegate
            hostViewController.presentViewController(
                pickerController,
                animated = true,
                completion = spinner::stopAnimating,
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun showWindow() {
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
        newWindow.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(SCRIM_ALPHA)
        newWindow.makeKeyAndVisible()
        window = newWindow
    }

    private fun tearDownWindow() {
        window?.setHidden(true)
        window?.rootViewController = null
        window = null
        previousKeyWindow?.makeKeyAndVisible()
        previousKeyWindow = null
        pickerController.delegate = null
        delegate = null
    }

    private suspend fun encodeToTempFile(image: UIImage): PlatformFile? = withContext(Dispatchers.Default) {
        val data = UIImageJPEGRepresentation(image, JPEG_QUALITY) ?: return@withContext null
        val path = NSTemporaryDirectory() + NSUUID().UUIDString + ".jpg"
        if (data.writeToFile(path, atomically = true)) {
            PlatformFile(NSURL.fileURLWithPath(path))
        } else {
            null
        }
    }
}

private class CameraDelegate(
    private val onFinished: (UIImage?) -> Unit,
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true) {
            onFinished(image)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            onFinished(null)
        }
    }
}
