package com.quare.bibleplanner.feature.deleteprogress.di

import com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel.DeleteAllProgressViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val deleteProgressModule = module {
    // Presentation
    viewModelOf(::DeleteAllProgressViewModel)
}
