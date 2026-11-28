package com.quare.bibleplanner.core.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

/*
 * Why: screens move to each platform's native toolkit one at a time (docs/architecture/native-ui-migration.md),
 * so an app binds this to draw the routes it already migrated, and null falls back to the shared entry.
 */
fun interface NativeEntryProvider {
    fun entryFor(key: NavKey): NavEntry<NavKey>?
}
