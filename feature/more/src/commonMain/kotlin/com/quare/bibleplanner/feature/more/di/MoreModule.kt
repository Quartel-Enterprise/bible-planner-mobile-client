package com.quare.bibleplanner.feature.more.di

import com.quare.bibleplanner.feature.more.presentation.viewmodel.MoreViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moreModule = module {
    viewModelOf(::MoreViewModel)
}
