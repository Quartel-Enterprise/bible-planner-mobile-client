package com.quare.bibleplanner.core.provider.platform.di

import com.quare.bibleplanner.core.provider.platform.domain.usecase.IsDebugBuild
import com.quare.bibleplanner.core.provider.platform.notification.IosNotificationPermissionRequester
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

internal actual val platformReviewModule: Module = module {
}

internal actual val platformNotificationPermissionModule: Module = module {
    factoryOf(::IosNotificationPermissionRequester).bind<NotificationPermissionRequester>()
}

@OptIn(ExperimentalNativeApi::class)
internal actual val platformDebugBuildModule: Module = module {
    factory<IsDebugBuild> { IsDebugBuild { Platform.isDebugBinary } }
}
