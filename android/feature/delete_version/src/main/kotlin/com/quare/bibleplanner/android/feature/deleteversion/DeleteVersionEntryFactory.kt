package com.quare.bibleplanner.android.feature.deleteversion

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute

internal class DeleteVersionEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? DeleteVersionNavRoute)?.toNavEntry()
}
