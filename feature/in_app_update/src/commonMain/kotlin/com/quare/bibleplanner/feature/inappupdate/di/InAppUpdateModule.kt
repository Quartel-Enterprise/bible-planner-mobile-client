package com.quare.bibleplanner.feature.inappupdate.di

import com.quare.bibleplanner.feature.inappupdate.data.UpdatePromptPreferencesImpl
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptPreferences
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ShowUpdatePrompt
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl.RequestUpdatePromptIfNeededUseCase
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.impl.ShowUpdatePromptUseCase
import com.quare.bibleplanner.feature.inappupdate.presentation.InAppUpdateDownloadViewModel
import com.quare.bibleplanner.feature.inappupdate.presentation.InAppUpdateViewModel
import com.quare.bibleplanner.feature.inappupdate.presentation.UpdateDownloadedViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val inAppUpdateModule = module {
    includes(platformInAppUpdateModule)
    singleOf(::UpdatePromptPreferencesImpl).bind<UpdatePromptPreferences>()
    factoryOf(::ShowUpdatePromptUseCase).bind<ShowUpdatePrompt>()
    factoryOf(::RequestUpdatePromptIfNeededUseCase).bind<RequestUpdatePromptIfNeeded>()
    viewModelOf(::InAppUpdateViewModel)
    viewModelOf(::UpdateDownloadedViewModel)
    viewModelOf(::InAppUpdateDownloadViewModel)
}

internal expect val platformInAppUpdateModule: Module
