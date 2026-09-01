package com.quare.bibleplanner.core.provider.platform.di

import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermission
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermissionUseCase
import com.quare.bibleplanner.core.provider.platform.getPlatform
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val platformModule = module {
    includes(platformReviewModule)
    includes(platformNotificationPermissionModule)
    includes(platformDebugBuildModule)
    factoryOf(::getPlatform)
    factoryOf(::GetAppStoreLinkUseCase)
    factoryOf(::RequestDownloadNotificationPermissionUseCase).bind<RequestDownloadNotificationPermission>()
}

internal expect val platformReviewModule: Module

internal expect val platformNotificationPermissionModule: Module

internal expect val platformDebugBuildModule: Module
