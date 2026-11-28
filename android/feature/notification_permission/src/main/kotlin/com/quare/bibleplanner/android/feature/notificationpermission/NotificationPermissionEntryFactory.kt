package com.quare.bibleplanner.android.feature.notificationpermission

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute

internal class NotificationPermissionEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? NotificationPermissionNavRoute)?.toNavEntry()
}
