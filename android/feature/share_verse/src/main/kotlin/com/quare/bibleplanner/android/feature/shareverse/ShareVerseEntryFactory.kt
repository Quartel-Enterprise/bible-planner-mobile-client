package com.quare.bibleplanner.android.feature.shareverse

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute

internal class ShareVerseEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = when (key) {
        is ShareVerseNavRoute -> key.toNavEntry()
        is ShareVerseImageNavRoute -> key.toNavEntry()
        else -> null
    }
}
