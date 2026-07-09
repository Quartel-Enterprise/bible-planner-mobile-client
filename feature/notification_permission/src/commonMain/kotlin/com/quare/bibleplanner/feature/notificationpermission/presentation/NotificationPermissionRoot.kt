package com.quare.bibleplanner.feature.notificationpermission.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

expect fun EntryProviderScope<NavKey>.notificationPermission(onNavigateBack: () -> Unit)
