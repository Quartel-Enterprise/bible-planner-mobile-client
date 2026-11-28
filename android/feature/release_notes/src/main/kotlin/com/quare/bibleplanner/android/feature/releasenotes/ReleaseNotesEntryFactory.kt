package com.quare.bibleplanner.android.feature.releasenotes

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute

internal class ReleaseNotesEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? ReleaseNotesNavRoute)?.toNavEntry()
}
