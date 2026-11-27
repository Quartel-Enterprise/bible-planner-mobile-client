package com.quare.bibleplanner.feature.applanguage.di

import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import org.koin.dsl.module

val webAppLanguageModule = module {
    factory<ApplyLocale> { ApplyLocale { } }
}
