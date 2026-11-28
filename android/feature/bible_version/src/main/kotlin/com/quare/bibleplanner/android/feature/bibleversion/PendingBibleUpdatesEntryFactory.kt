package com.quare.bibleplanner.android.feature.bibleversion

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.PendingBibleUpdatesNavRoute

internal class PendingBibleUpdatesEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? PendingBibleUpdatesNavRoute)?.toNavEntry()
}
