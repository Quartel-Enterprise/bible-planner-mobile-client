package com.quare.bibleplanner.android.feature.verseselection

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute

internal class VerseSelectionEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? VerseSelectionNavRoute)?.toNavEntry()
}
