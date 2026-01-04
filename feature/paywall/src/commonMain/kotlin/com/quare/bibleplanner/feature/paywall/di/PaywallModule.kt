package com.quare.bibleplanner.feature.paywall.di

import com.quare.bibleplanner.feature.paywall.presentation.factory.PaywallUiStateFactory
import com.quare.bibleplanner.feature.paywall.presentation.mapper.PaywallExceptionMapper
import com.quare.bibleplanner.feature.paywall.presentation.viewmodel.PaywallViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val paywallModule = module {
    // Presentation
    factoryOf(::PaywallExceptionMapper)
    factoryOf(::PaywallUiStateFactory)
    viewModelOf(::PaywallViewModel)
}
