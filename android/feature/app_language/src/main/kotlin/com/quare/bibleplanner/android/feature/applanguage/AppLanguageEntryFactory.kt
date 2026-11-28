package com.quare.bibleplanner.android.feature.applanguage

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute

internal class AppLanguageEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? AppLanguageNavRoute)?.toNavEntry()
}
