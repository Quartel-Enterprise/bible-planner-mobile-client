package com.quare.bibleplanner.ui.utils

import androidx.compose.ui.platform.ClipEntry

fun String.removeAccents(): String {
    val accents = "ÁÉÍÓÚáéíóúÂÊÔâêôÃÕãõÀàÇçÑñÜü"
    val replacements = "AEIOUaeiouAEOaeoAOaoAaCcNnUu"
    val sb = StringBuilder()
    for (char in this) {
        val index = accents.indexOf(char)
        if (index != -1) {
            sb.append(replacements[index])
        } else {
            sb.append(char)
        }
    }
    return sb.toString()
}

expect fun String.toClipEntry(): ClipEntry
