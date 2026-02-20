package com.quare.bibleplanner.core.utils.locale

import java.util.Locale

actual fun getCurrentLanguage(): Language {
    val locale = Locale.getDefault()
    return when {
        locale.language == "pt" && locale.country == "BR" -> Language.PORTUGUESE_BRAZIL
        locale.language.startsWith("es") -> Language.SPANISH
        else -> Language.ENGLISH
    }
}
