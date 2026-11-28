package com.quare.bibleplanner.android.feature.paywall

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.PaywallNavRoute

internal class PaywallEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? PaywallNavRoute)?.toNavEntry()
}
