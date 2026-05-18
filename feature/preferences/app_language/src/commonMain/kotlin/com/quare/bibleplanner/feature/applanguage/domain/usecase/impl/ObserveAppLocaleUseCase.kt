package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import com.quare.bibleplanner.feature.applanguage.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveAppLocale

internal class ObserveAppLocaleUseCase(
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val applyLocale: ApplyLocale,
) : ObserveAppLocale {
    override suspend fun invoke() {
        getAppLanguageFlow().collect { language ->
            applyLocale(language)
        }
    }
}
