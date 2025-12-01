package com.quare.bibleplanner.core.provider.platform.di

import com.quare.bibleplanner.core.provider.platform.getPlatform
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val platformModule = module {
    factoryOf(::getPlatform)
}
