package com.quare.bibleplanner.core.utils.di

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import org.koin.dsl.module

val utilsModule = module {
    single { ApplicationScope() }
}
