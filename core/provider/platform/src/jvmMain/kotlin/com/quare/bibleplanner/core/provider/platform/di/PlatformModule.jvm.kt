package com.quare.bibleplanner.core.provider.platform.di

import com.quare.bibleplanner.core.provider.platform.domain.usecase.IsDebugBuild
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestInAppReview
import com.quare.bibleplanner.core.provider.platform.isDebugBuild
import com.quare.bibleplanner.core.provider.platform.notification.JvmNotificationPermissionRequester
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformReviewModule: Module = module {
    factory<RequestInAppReview> { RequestInAppReview { false } }
}

internal actual val platformNotificationPermissionModule: Module = module {
    factoryOf(::JvmNotificationPermissionRequester).bind<NotificationPermissionRequester>()
}

internal actual val platformDebugBuildModule: Module = module {
    factory<IsDebugBuild> { IsDebugBuild(::isDebugBuild) }
}
