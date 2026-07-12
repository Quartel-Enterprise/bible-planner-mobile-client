package com.quare.bibleplanner.core.provider.platform.di

import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.core.provider.platform.getPlatform
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val platformModule = module {
    includes(platformReviewModule)
    factoryOf(::getPlatform)
    factoryOf(::GetAppStoreLinkUseCase)
}

internal expect val platformReviewModule: Module
