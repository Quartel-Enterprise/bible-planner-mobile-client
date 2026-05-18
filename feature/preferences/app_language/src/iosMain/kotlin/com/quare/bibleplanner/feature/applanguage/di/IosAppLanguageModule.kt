package com.quare.bibleplanner.feature.applanguage.di

import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import com.quare.bibleplanner.feature.applanguage.presentation.IosApplyLocale
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val iosAppLanguageModule = module {
    factoryOf(::IosApplyLocale).bind<ApplyLocale>()
}
