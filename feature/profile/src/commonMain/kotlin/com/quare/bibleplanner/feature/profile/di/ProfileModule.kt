package com.quare.bibleplanner.feature.profile.di

import com.quare.bibleplanner.feature.profile.domain.usecase.GetInstagramUrlUseCase
import com.quare.bibleplanner.feature.profile.domain.usecase.GetSelectedVersionDownloadedChaptersFlowUseCase
import com.quare.bibleplanner.feature.profile.domain.usecase.ObserveShowDonateOptionUseCase
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileUiStateFactory
import com.quare.bibleplanner.feature.profile.presentation.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureProfileModule = module {
    // Domain
    factoryOf(::GetInstagramUrlUseCase)
    factoryOf(::ObserveShowDonateOptionUseCase)
    factoryOf(::GetSelectedVersionDownloadedChaptersFlowUseCase)

    // Presentation
    factoryOf(::ProfileUiStateFactory)
    viewModelOf(::ProfileViewModel)
}
