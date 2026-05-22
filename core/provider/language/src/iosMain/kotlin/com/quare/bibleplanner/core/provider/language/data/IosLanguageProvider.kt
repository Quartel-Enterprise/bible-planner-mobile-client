package com.quare.bibleplanner.core.provider.language.data

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import platform.Foundation.NSGlobalDomain
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

internal class IosLanguageProvider : LanguageProvider {
    override fun getDeviceLanguage(): Language {
        val globalPrefs = NSUserDefaults.standardUserDefaults.persistentDomainForName(NSGlobalDomain)

        @Suppress("UNCHECKED_CAST")
        val systemLanguages = globalPrefs?.get("AppleLanguages") as? List<String>
        return systemLanguages?.firstOrNull()?.toLanguage() ?: Language.ENGLISH
    }

    override fun getAppLanguage(): Language {
        val languageTag = NSLocale.preferredLanguages.firstOrNull() as? String ?: return Language.ENGLISH
        return languageTag.toLanguage()
    }

    private fun String.toLanguage(): Language {
        val normalized = replace("_", "-")
        val parts = normalized.split("-")
        val lang = parts.getOrNull(0).orEmpty()
        val region = parts.getOrNull(1).orEmpty()
        return when {
            lang == "pt" && region == "BR" -> Language.PORTUGUESE_BRAZIL
            lang.startsWith("es") -> Language.SPANISH
            else -> Language.ENGLISH
        }
    }
}
