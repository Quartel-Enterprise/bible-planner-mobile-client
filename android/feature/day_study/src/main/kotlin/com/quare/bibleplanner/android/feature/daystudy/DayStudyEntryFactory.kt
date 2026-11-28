package com.quare.bibleplanner.android.feature.daystudy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute

internal class DayStudyEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? DayStudyNavRoute)?.toNavEntry()
}
