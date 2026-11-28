package com.quare.bibleplanner.android.feature.deletenotes

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute

internal class DeleteNotesEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? DeleteNotesRoute)?.toNavEntry()
}
