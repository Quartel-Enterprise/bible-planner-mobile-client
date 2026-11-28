package com.quare.bibleplanner.android.feature.dayreadingcomplete

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute

internal class DayReadingCompleteEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? DayReadingCompleteNavRoute)?.toNavEntry()
}
