package com.quare.bibleplanner.ui.utils

import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

actual fun String.toClipEntry(): ClipEntry = ClipEntry(StringSelection(this))
