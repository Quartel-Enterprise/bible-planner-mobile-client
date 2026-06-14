package com.quare.bibleplanner.feature.loginwarning.di

import com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel.LoginWarningViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginWarningModule = module {
    // Presentation
    viewModelOf(::LoginWarningViewModel)
}
