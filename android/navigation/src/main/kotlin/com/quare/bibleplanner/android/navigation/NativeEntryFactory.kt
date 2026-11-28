package com.quare.bibleplanner.android.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

/*
 * Why: each native screen module lists its factory in META-INF/services, and the app finds them all with
 * ServiceLoader, so adding a screen never edits a shared file and screens can land in parallel PRs.
 */
fun interface NativeEntryFactory {
    fun entryFor(key: NavKey): NavEntry<NavKey>?
}
