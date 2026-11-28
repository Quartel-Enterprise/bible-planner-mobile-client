package com.quare.bibleplanner.android.feature.themeselection

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.ThemeNavRoute

internal class ThemeSelectionEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? ThemeNavRoute)?.toNavEntry()
}
