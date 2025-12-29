package com.quare.bibleplanner.feature.onboardingstartdate.di

import com.quare.bibleplanner.feature.onboardingstartdate.data.repository.OnboardingStartDateRepositoryImpl
import com.quare.bibleplanner.feature.onboardingstartdate.domain.repository.OnboardingStartDateRepository
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.viewmodel.OnboardingStartDateViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingStartDateModule = module {
    // Data
    singleOf(::OnboardingStartDateRepositoryImpl) { bind<OnboardingStartDateRepository>() }

    // ViewModel
    viewModelOf(::OnboardingStartDateViewModel)
}
