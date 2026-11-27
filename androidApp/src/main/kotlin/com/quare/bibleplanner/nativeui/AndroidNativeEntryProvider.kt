package com.quare.bibleplanner.nativeui

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.feature.deletenotes.toNavEntry
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.navigation.NativeEntryProvider

internal class AndroidNativeEntryProvider : NativeEntryProvider {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = if (key is DeleteNotesRoute) key.toNavEntry() else null
}
