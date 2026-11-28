package com.quare.bibleplanner.android.feature.main

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.MainNavRoute

internal class MainEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? MainNavRoute)?.toNavEntry()
}
