package com.quare.bibleplanner.nativeui

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory
import com.quare.bibleplanner.core.navigation.NativeEntryProvider

internal class AndroidNativeEntryProvider(
    private val factories: List<NativeEntryFactory>,
) : NativeEntryProvider {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = factories.firstNotNullOfOrNull { factory ->
        factory.entryFor(key)
    }
}
