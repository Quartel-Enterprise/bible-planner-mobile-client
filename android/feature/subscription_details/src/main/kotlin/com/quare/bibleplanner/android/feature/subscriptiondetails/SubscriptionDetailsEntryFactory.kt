package com.quare.bibleplanner.android.feature.subscriptiondetails

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute

internal class SubscriptionDetailsEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? SubscriptionDetailsNavRoute)?.toNavEntry()
}
