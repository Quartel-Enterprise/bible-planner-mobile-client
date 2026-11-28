package com.quare.bibleplanner.android.feature.accountdetails

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute

internal class AccountDetailsEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? AccountDetailsNavRoute)?.toNavEntry()
}
