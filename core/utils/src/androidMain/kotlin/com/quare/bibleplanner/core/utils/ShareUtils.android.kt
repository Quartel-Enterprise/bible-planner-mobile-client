package com.quare.bibleplanner.core.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.koin.core.context.GlobalContext
import java.io.File

actual fun shareContent(
    message: String,
    imageBytes: ByteArray?,
) {
    val context = GlobalContext.get().get<Context>()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (imageBytes != null) "image/png" else "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)

        if (imageBytes != null) {
            val cachePath = File(context.cacheDir, "shared_images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_image.png")
            file.writeBytes(imageBytes)

            val contentUri = FileProvider.getUriForFile(
                context,
                "com.quare.bibleplanner.fileprovider",
                file,
            )
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val chooser = Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
