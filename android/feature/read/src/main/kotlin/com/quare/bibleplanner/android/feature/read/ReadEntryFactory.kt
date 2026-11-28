package com.quare.bibleplanner.android.feature.read

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.android.navigation.NativeEntryFactory

/*
 * Why: the reader moves in two pull requests; this one only adds the verse list's building blocks,
 * so the shared reader keeps drawing ReadNavRoute until the screen itself lands and returns its entry here.
 */
internal class ReadEntryFactory : NativeEntryFactory {
    override fun entryFor(key: NavKey): NavEntry<NavKey>? = null
}
