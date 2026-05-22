package com.quare.bibleplanner.core.provider.language.data

import android.content.res.Resources
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import java.util.Locale

internal class AndroidLanguageProvider : LanguageProvider {
    override fun getDeviceLanguage(): Language {
        val locale = Resources.getSystem().configuration.locales[0]
        return locale.toLanguage()
    }

    override fun getAppLanguage(): Language = Locale.getDefault().toLanguage()

    private fun Locale.toLanguage(): Language = when {
        language == "pt" && country == "BR" -> Language.PORTUGUESE_BRAZIL
        language.startsWith("es") -> Language.SPANISH
        else -> Language.ENGLISH
    }
}
