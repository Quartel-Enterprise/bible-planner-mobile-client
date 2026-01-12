package com.quare.bibleplanner.core.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun shareContent(
    message: String,
    imageBytes: ByteArray?,
) {
    val items = mutableListOf<Any>(message)

    if (imageBytes != null) {
        val nsData = imageBytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = imageBytes.size.toULong(),
            )
        }
        val image = UIImage.imageWithData(nsData)
        if (image != null) {
            items.add(image)
        }
    }

    val activityController = UIActivityViewController(items, null)

    val window = UIApplication.sharedApplication.keyWindow
    val rootViewController = window?.rootViewController

    rootViewController?.presentViewController(activityController, animated = true, completion = null)
}
