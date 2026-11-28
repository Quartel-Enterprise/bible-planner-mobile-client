package com.quare.bibleplanner.android.feature.loginsyncnudge

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute

internal class LoginSyncNudgeEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? LoginSyncNudgeNavRoute)?.toNavEntry()
}
