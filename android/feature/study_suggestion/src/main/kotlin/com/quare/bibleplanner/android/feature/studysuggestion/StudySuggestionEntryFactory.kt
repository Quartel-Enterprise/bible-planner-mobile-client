package com.quare.bibleplanner.android.feature.studysuggestion

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.StudySuggestionNavRoute

internal class StudySuggestionEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? StudySuggestionNavRoute)?.toNavEntry()
}
