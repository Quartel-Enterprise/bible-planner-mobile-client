package com.quare.bibleplanner.ui.utils

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun String.toClipEntry(): ClipEntry = ClipData.newPlainText(this, this).toClipEntry()
