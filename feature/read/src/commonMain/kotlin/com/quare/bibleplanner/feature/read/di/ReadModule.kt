package com.quare.bibleplanner.feature.read.di

import com.quare.bibleplanner.feature.read.presentation.ReadViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureReadModule = module {
    viewModelOf(::ReadViewModel)
}
