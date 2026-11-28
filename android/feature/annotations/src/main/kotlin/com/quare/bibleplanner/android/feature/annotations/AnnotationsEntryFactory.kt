package com.quare.bibleplanner.android.feature.annotations

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.AnnotationsNavRoute

internal class AnnotationsEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? AnnotationsNavRoute)?.toNavEntry()
}
