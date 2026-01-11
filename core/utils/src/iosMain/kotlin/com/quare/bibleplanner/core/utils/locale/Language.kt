package com.quare.bibleplanner.core.utils.locale

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.regionCode

actual fun getCurrentLanguage(): AppLanguage {
    val locale = NSLocale.currentLocale
    val language = locale.languageCode
    val region = locale.regionCode
    return when {
        language == "pt" && region == "BR" -> AppLanguage.PORTUGUESE_BRAZIL
        language.startsWith("es") -> AppLanguage.SPANISH
        else -> AppLanguage.ENGLISH
    }
}
