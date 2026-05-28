package com.quare.bibleplanner.feature.logout.di

import com.quare.bibleplanner.feature.logout.presentation.viewmodel.LogoutViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val logoutModule = module {
    viewModelOf(::LogoutViewModel)
}
