package com.quare.bibleplanner.core.provider.language.data

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language

internal class WebLanguageProvider : LanguageProvider {
    override fun getDeviceLanguage(): Language = getNavigatorLanguage().toLanguage()

    override fun getAppLanguage(): Language = getNavigatorLanguage().toLanguage()

    private fun String.toLanguage(): Language = when {
        equals(PORTUGUESE_BRAZIL_TAG, ignoreCase = true) -> Language.PORTUGUESE_BRAZIL
        startsWith(SPANISH_PREFIX) -> Language.SPANISH
        else -> Language.ENGLISH
    }

    private companion object {
        const val PORTUGUESE_BRAZIL_TAG = "pt-BR"
        const val SPANISH_PREFIX = "es"
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun getNavigatorLanguage(): String = js("navigator.language")
