package com.quare.bibleplanner.core.provider.language.data

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import java.util.Locale

internal class JvmLanguageProvider : LanguageProvider {
    override fun getDeviceLanguage(): Language {
        val language = System.getProperty("user.language").orEmpty()
        val country = System.getProperty("user.country").orEmpty()
        return toLanguage(language, country)
    }

    override fun getAppLanguage(): Language {
        val locale = Locale.getDefault()
        return toLanguage(locale.language, locale.country)
    }

    private fun toLanguage(
        language: String,
        country: String,
    ): Language = when {
        language == "pt" && country == "BR" -> Language.PORTUGUESE_BRAZIL
        language.startsWith("es") -> Language.SPANISH
        else -> Language.ENGLISH
    }
}
