package com.quare.bibleplanner.feature.loginsyncnudge.di

import com.quare.bibleplanner.feature.loginsyncnudge.presentation.viewmodel.LoginSyncNudgeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginSyncNudgeModule = module {
    // Presentation
    viewModelOf(::LoginSyncNudgeViewModel)
}
