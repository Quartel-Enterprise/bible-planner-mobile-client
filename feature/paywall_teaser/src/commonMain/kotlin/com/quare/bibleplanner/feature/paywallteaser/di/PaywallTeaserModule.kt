package com.quare.bibleplanner.feature.paywallteaser.di

import com.quare.bibleplanner.feature.paywallteaser.presentation.viewmodel.PaywallTeaserViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val paywallTeaserModule = module {
    viewModelOf(::PaywallTeaserViewModel)
}
