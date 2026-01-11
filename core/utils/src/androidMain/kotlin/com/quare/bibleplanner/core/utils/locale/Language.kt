package com.quare.bibleplanner.core.utils.locale

import java.util.Locale

actual fun getCurrentLanguage(): AppLanguage {
    val locale = Locale.getDefault()
    return when {
        locale.language == "pt" && locale.country == "BR" -> AppLanguage.PORTUGUESE_BRAZIL
        locale.language.startsWith("es") -> AppLanguage.SPANISH
        else -> AppLanguage.ENGLISH
    }
}
