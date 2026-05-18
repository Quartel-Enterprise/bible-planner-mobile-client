package com.quare.bibleplanner.feature.applanguage.di

import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import com.quare.bibleplanner.feature.applanguage.presentation.AndroidApplyLocale
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidAppLanguageModule = module {
    factoryOf(::AndroidApplyLocale).bind<ApplyLocale>()
}
