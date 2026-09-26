package com.quare.bibleplanner.core.preferences.materialyou.di

import com.quare.bibleplanner.core.preferences.materialyou.data.repository.MaterialYouRepositoryImpl
import com.quare.bibleplanner.core.preferences.materialyou.domain.model.MaterialYouUseCases
import com.quare.bibleplanner.core.preferences.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.ObserveDynamicColorsSync
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.impl.GetIsDynamicColorsEnabledFlowUseCase
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.impl.ObserveDynamicColorsSyncUseCase
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.impl.SetIsDynamicColorsEnabledUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val materialYouModule = module {
    includes(MaterialYouPlatformModule)

    // Data
    singleOf(::MaterialYouRepositoryImpl).bind<MaterialYouRepository>()

    // Domain
    factoryOf(::MaterialYouUseCases)
    factoryOf(::GetIsDynamicColorsEnabledFlowUseCase).bind<GetIsDynamicColorsEnabledFlow>()
    factoryOf(::SetIsDynamicColorsEnabledUseCase).bind<SetIsDynamicColorsEnabled>()
    factoryOf(::ObserveDynamicColorsSyncUseCase).bind<ObserveDynamicColorsSync>()
}
