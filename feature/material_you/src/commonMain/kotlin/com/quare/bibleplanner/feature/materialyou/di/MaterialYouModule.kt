package com.quare.bibleplanner.feature.materialyou.di

import com.quare.bibleplanner.feature.materialyou.presentation.viewmodel.AndroidColorSchemeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureMaterialYouModule = module {
    viewModelOf(::AndroidColorSchemeViewModel)
}
