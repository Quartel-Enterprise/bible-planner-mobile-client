package com.quare.bibleplanner.feature.unlockpremium.di

import com.quare.bibleplanner.feature.unlockpremium.domain.model.usecase.GetSubscriptionPlansUseCase
import com.quare.bibleplanner.feature.unlockpremium.presentation.factory.UnlockPremiumUiStateFactory
import com.quare.bibleplanner.feature.unlockpremium.presentation.viewmodel.UnlockPremiumViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val unlockPremiumModule = module {
    // Use Case
    factoryOf(::GetSubscriptionPlansUseCase)

    // Presentation
    viewModelOf(::UnlockPremiumViewModel)
    factoryOf(::UnlockPremiumUiStateFactory)
}
