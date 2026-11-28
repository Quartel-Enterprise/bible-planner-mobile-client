package com.quare.bibleplanner.android.feature.bookdetails

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute

internal class BookDetailsEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = (key as? BookDetailsNavRoute)?.toNavEntry()
}
