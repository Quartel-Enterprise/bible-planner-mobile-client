package com.quare.bibleplanner.android.feature.readingplan

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination

internal class ReadingPlanEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? MainNavRouteDestination.Plans)?.toNavEntry()
}
