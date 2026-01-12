package com.quare.bibleplanner.feature.donation.di

import com.quare.bibleplanner.feature.donation.presentation.DonationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val donationModule = module {
    viewModelOf(::DonationViewModel)
}
