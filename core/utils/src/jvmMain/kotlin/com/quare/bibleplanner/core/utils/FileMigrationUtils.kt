package com.quare.bibleplanner.core.utils

import java.io.File

fun migrateLegacyFileIfPresent(
    legacyFile: File,
    targetFile: File,
) {
    if (!legacyFile.exists() || targetFile.exists()) return
    legacyFile.copyTo(targetFile)
    legacyFile.delete()
}
