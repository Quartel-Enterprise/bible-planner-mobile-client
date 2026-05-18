package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetAppLanguageFlow
import kotlinx.coroutines.flow.first

suspend fun initAppLocale(
    getAppLanguageFlow: GetAppLanguageFlow,
    applyLocale: ApplyLocale,
) {
    val language = getAppLanguageFlow().first()
    applyLocale(language)
}
