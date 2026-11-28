package com.quare.bibleplanner.android.feature.logout

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.LogoutNavRoute

internal class LogoutEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? LogoutNavRoute)?.toNavEntry()
}
