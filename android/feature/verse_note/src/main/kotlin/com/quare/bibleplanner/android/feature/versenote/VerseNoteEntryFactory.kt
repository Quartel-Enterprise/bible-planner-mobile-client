package com.quare.bibleplanner.android.feature.versenote

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute

internal class VerseNoteEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? VerseNoteNavRoute)?.toNavEntry()
}
