package com.quare.bibleplanner.android.feature.deleteaccount

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.DeleteAccountNavRoute

internal class DeleteAccountEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? DeleteAccountNavRoute)?.toNavEntry()
}
