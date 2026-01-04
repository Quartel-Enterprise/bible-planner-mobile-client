package com.quare.bibleplanner.feature.main.di

import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    viewModelOf(::MainScreenViewModel)
}
