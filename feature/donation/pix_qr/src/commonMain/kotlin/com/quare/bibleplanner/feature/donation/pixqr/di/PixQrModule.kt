package com.quare.bibleplanner.feature.donation.pixqr.di

import com.quare.bibleplanner.feature.donation.pixqr.presentation.PixQrViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pixQrModule = module {
    viewModelOf(::PixQrViewModel)
}
