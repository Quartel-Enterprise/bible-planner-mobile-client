package com.quare.bibleplanner.feature.notificationpermission.di

import com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel.NotificationPermissionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notificationPermissionModule = module {
    viewModelOf(::NotificationPermissionViewModel)
}
