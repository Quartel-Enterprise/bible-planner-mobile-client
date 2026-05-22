package com.quare.bibleplanner.core.provider.language.domain.provider

import com.quare.bibleplanner.core.utils.locale.Language

interface LanguageProvider {
    fun getDeviceLanguage(): Language

    fun getAppLanguage(): Language
}
