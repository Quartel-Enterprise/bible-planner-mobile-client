package com.quare.bibleplanner.android.feature.congrats

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.CongratsNavRoute

internal class CongratsEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? CongratsNavRoute)?.toNavEntry()
}
