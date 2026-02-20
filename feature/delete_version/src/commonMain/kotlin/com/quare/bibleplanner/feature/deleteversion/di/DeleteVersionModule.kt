package com.quare.bibleplanner.feature.deleteversion.di

import com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel.DeleteVersionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val deleteVersionModule = module {
    viewModelOf(::DeleteVersionViewModel)
}
