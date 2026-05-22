package com.quare.bibleplanner.feature.donation.di

import com.quare.bibleplanner.feature.donation.presentation.DonationViewModel
import com.quare.bibleplanner.feature.donation.presentation.factory.DonationUiStateFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val donationModule = module {
    factoryOf(::DonationUiStateFactory)
    viewModelOf(::DonationViewModel)
}
