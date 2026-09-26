package com.quare.bibleplanner.feature.profile.fake

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language

internal class FakeLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = appLanguage
}
