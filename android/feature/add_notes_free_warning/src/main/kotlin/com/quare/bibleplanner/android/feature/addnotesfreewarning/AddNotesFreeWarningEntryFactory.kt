package com.quare.bibleplanner.android.feature.addnotesfreewarning

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute

internal class AddNotesFreeWarningEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? AddNotesFreeWarningNavRoute)?.toNavEntry()
}
