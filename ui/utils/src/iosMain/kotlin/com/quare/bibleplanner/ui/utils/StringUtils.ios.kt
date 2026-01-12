@file:OptIn(ExperimentalComposeUiApi::class)

package com.quare.bibleplanner.ui.utils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

actual fun String.toClipEntry(): ClipEntry = ClipEntry.withPlainText(this)
