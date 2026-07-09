package com.quare.bibleplanner.core.provider.platform.domain.usecase

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.locale.Language

class GetAppStoreLinkUseCase(
    private val platform: Platform,
    private val languageProvider: LanguageProvider,
) {
    operator fun invoke(): String = when (platform) {
        Platform.Ios -> "https://apps.apple.com/${iosLocale()}/app/bible-planner-reading-plans/$APP_STORE_ID"
        Platform.Android -> "https://play.google.com/store/apps/details?id=$ANDROID_PACKAGE_NAME&hl=${androidLocale()}"
        is Platform.Desktop -> DESKTOP_URL
    }

    private fun iosLocale(): String = when (languageProvider.getAppLanguage()) {
        Language.PORTUGUESE_BRAZIL -> "br"
        Language.SPANISH -> "es"
        Language.ENGLISH -> "us"
    }

    private fun androidLocale(): String = when (languageProvider.getAppLanguage()) {
        Language.PORTUGUESE_BRAZIL -> "pt-BR"
        Language.SPANISH -> "es"
        Language.ENGLISH -> "en"
    }

    companion object {
        private const val APP_STORE_ID = "id6756151777"
        private const val ANDROID_PACKAGE_NAME = "com.quare.bibleplanner"
        private const val DESKTOP_URL = "https://bibleplanner.app"
    }
}
