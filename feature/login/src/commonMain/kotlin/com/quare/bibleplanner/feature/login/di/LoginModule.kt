package com.quare.bibleplanner.feature.login.di

import com.quare.bibleplanner.feature.login.presentation.LoginViewModel
import com.quare.bibleplanner.feature.login.presentation.factory.LoginUiStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    factoryOf(::LoginUiStateFactory)
    viewModelOf(::LoginViewModel)
}
