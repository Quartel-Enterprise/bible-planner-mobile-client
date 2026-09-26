package com.quare.bibleplanner.core.provider.language.fake

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language

internal class FixedLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = appLanguage
}
