package com.quare.bibleplanner.core.provider.platform.di

import com.quare.bibleplanner.core.provider.platform.notification.IosNotificationPermissionRequester
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformReviewModule: Module = module {
}

internal actual val platformNotificationPermissionModule: Module = module {
    factoryOf(::IosNotificationPermissionRequester).bind<NotificationPermissionRequester>()
}
