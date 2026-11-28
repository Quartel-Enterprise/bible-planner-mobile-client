package com.quare.bibleplanner.android.feature.paywallteaser

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute

internal class PaywallTeaserEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? PaywallTeaserNavRoute)?.toNavEntry()
}
