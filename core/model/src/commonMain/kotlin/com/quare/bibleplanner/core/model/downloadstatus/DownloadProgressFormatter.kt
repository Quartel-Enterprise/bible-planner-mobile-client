package com.quare.bibleplanner.core.model.downloadstatus

import kotlin.math.roundToInt

fun formatDownloadProgress(progress: Float): String {
    val rounded = (progress * 10000).roundToInt()
    val intPart = rounded / 100
    val fracPart = rounded % 100
    return if (fracPart == 0) "$intPart" else "$intPart.${fracPart.toString().padStart(2, '0')}"
}
