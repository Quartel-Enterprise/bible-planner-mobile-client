package com.quare.bibleplanner.feature.applanguage.di

import com.quare.bibleplanner.feature.applanguage.domain.ApplyLocale
import com.quare.bibleplanner.feature.applanguage.presentation.JvmApplyLocale
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val jvmAppLanguageModule = module {
    factoryOf(::JvmApplyLocale).bind<ApplyLocale>()
}
