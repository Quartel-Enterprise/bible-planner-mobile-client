package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    viewModelOf(::LoginViewModel)
}
