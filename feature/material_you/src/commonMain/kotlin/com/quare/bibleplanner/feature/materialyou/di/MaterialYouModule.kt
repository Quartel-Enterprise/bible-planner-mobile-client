package com.quare.bibleplanner.feature.materialyou.di

import com.quare.bibleplanner.feature.materialyou.data.repository.MaterialYouRepositoryImpl
import com.quare.bibleplanner.feature.materialyou.domain.model.MaterialYouUseCases
import com.quare.bibleplanner.feature.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import com.quare.bibleplanner.feature.materialyou.domain.usecase.impl.GetIsDynamicColorsEnabledFlowUseCase
import com.quare.bibleplanner.feature.materialyou.domain.usecase.impl.SetIsDynamicColorsEnabledUseCase
import com.quare.bibleplanner.feature.materialyou.presentation.viewmodel.AndroidColorSchemeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val materialYouModule = module {
    // Data
    singleOf(::MaterialYouRepositoryImpl).bind<MaterialYouRepository>()

    // Domain
    factoryOf(::MaterialYouUseCases)
    factoryOf(::GetIsDynamicColorsEnabledFlowUseCase).bind<GetIsDynamicColorsEnabledFlow>()
    factoryOf(::SetIsDynamicColorsEnabledUseCase).bind<SetIsDynamicColorsEnabled>()

    // Presentation
    viewModelOf(::AndroidColorSchemeViewModel)
}
