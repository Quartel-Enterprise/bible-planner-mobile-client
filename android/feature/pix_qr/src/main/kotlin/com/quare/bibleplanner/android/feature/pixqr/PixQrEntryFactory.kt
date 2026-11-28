package com.quare.bibleplanner.android.feature.pixqr

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.PixQrNavRoute

internal class PixQrEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? PixQrNavRoute)?.toNavEntry()
}
