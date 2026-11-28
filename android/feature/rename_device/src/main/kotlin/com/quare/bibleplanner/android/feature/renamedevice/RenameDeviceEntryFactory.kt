package com.quare.bibleplanner.android.feature.renamedevice

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute

internal class RenameDeviceEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? RenameDeviceNavRoute)?.toNavEntry()
}
