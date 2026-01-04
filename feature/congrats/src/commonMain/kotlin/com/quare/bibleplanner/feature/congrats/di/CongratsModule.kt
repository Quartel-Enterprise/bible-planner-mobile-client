package com.quare.bibleplanner.feature.congrats.di

import com.quare.bibleplanner.feature.congrats.presentation.viewmodel.CongratsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val congratsModule = module {
    viewModelOf(::CongratsViewModel)
}
