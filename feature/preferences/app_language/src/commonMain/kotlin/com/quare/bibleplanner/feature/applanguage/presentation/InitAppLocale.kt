package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import kotlinx.coroutines.flow.first

suspend fun initAppLocale(
    getAppLanguageFlow: GetAppLanguageFlow,
    applyLocale: ApplyLocale,
) {
    val language = getAppLanguageFlow().first()
    applyLocale(language)
}
