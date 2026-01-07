package com.quare.bibleplanner.feature.subscriptiondetails.di

import com.quare.bibleplanner.feature.subscriptiondetails.presentation.viewmodel.SubscriptionDetailsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val subscriptionDetailsModule = module {
    viewModelOf(::SubscriptionDetailsViewModel)
}
