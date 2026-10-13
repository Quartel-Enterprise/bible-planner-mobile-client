package com.quare.bibleplanner.core.inappupdate.di

import com.quare.bibleplanner.core.inappupdate.data.UpdatePromptPreferencesImpl
import com.quare.bibleplanner.core.inappupdate.domain.UpdatePromptPreferences
import com.quare.bibleplanner.core.inappupdate.domain.usecase.InstallDownloadedUpdate
import com.quare.bibleplanner.core.inappupdate.domain.usecase.RequestUpdatePromptIfNeeded
import com.quare.bibleplanner.core.inappupdate.domain.usecase.ShowUpdatePrompt
import com.quare.bibleplanner.core.inappupdate.domain.usecase.impl.InstallDownloadedUpdateUseCase
import com.quare.bibleplanner.core.inappupdate.domain.usecase.impl.RequestUpdatePromptIfNeededUseCase
import com.quare.bibleplanner.core.inappupdate.domain.usecase.impl.ShowUpdatePromptUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val inAppUpdateModule = module {
    includes(platformInAppUpdateModule)
    singleOf(::UpdatePromptPreferencesImpl).bind<UpdatePromptPreferences>()
    singleOf(::InstallDownloadedUpdateUseCase).bind<InstallDownloadedUpdate>()
    factoryOf(::ShowUpdatePromptUseCase).bind<ShowUpdatePrompt>()
    factoryOf(::RequestUpdatePromptIfNeededUseCase).bind<RequestUpdatePromptIfNeeded>()
}

internal expect val platformInAppUpdateModule: Module
