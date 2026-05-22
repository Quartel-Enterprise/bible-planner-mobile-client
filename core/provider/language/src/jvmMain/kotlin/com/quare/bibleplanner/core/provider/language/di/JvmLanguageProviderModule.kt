package com.quare.bibleplanner.core.provider.language.di

import com.quare.bibleplanner.core.provider.language.data.JvmLanguageProvider
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val jvmLanguageProviderModule = module {
    factoryOf(::JvmLanguageProvider).bind<LanguageProvider>()
}
